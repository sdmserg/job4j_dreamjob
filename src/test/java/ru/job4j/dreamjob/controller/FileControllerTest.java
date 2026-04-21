package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileControllerTest {

    private FileService fileService;

    private FileController fileController;

    @BeforeEach
    public void init() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
    }

    @Test
    public void whenFileExistsThenReturnContent() {
        int fileId = 5;
        var fileDto = new FileDto("testFile.img", new byte[] {1, 2, 3});
        when(fileService.getFileById(fileId)).thenReturn(Optional.of(fileDto));

        var response = fileController.getById(fileId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(fileDto.getContent());
    }

    @Test
    public void whenFileNotFoundThenReturn404NotFound() {
        int fileId = 5;
        when(fileService.getFileById(fileId)).thenReturn(Optional.empty());

        var response = fileController.getById(fileId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}