package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class CandidateControllerTest {

    private CandidateService candidateService;

    private CityService cityService;

    private CandidateController candidateController;

    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestCandidatesListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "candidate1", "desc1", 1, 2);
        var candidate2 = new Candidate(2, "candidate2", "desc2", 3, 4);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var city3 = new City(3, "Екатеринбург");
        var expectedCities = List.of(city1, city2, city3);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationResumePage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "candidate", "desc", 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates/list");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenPostCandidateThrowsExceptionThenGetErrorPageWithMessage() {
        var candidate = new Candidate(1, "candidate", "desc", 1, 2);
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestCandidatePageThenGetCandidateyPageWithCity() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var city3 = new City(3, "Екатеринбург");
        var expectedCities = List.of(city1, city2, city3);
        when(cityService.findAll()).thenReturn(expectedCities);
        var candidate = new Candidate(1, "candidate", "desc", 1, 2);
        when(candidateService.findById(candidate.getId())).thenReturn(Optional.of(candidate));

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, candidate.getId());
        var actualCities = model.getAttribute("cities");
        var actualCandidate = model.getAttribute("candidate");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualCities).usingRecursiveComparison().isEqualTo(expectedCities);
    }

    @Test
    public void whenRequestCandidatePageByIdNotFoundThenGetErrorPageWithMessage() {
        int candidateId = 7;
        var expectedMessage = "Резюме с указанным идентификатором не найдена";
        when(candidateService.findById(candidateId)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, candidateId);
        var actualErrorMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualErrorMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenUpdateCandidateSuccessfullyThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "candidate", "desc", 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates/list");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdateCandidateFailedThenGetErrorPageWithMessage() {
        var candidate = new Candidate(1, "candidate", "desc", 1, 2);
        var expectedMessage = "Резюме с указанным идентификатором не найдено!";
        when(candidateService.update(any(Candidate.class), any(FileDto.class))).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualErrorMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualErrorMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenUpdateCandidateThrowsExceptionThenGetErrorPageWithMessage() {
        var candidate = new Candidate(1, "candidate", "desc", 1, 2);
        var expectedException = new RuntimeException("При обновлении резюме произошла ошибка");
        when(candidateService.update(any(Candidate.class), any(FileDto.class))).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenDeleteCandidateSuccessfullyThenGetCandidatePage() {
        int candidateId = 1;
        when(candidateService.deleteById(candidateId)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, candidateId);

        assertThat(view).isEqualTo("redirect:/candidates/list");
    }

    @Test
    public void whenDeleteCandidateFailedThenGetErrorPageWithMessage() {
        int candidateId = 1;
        var expectedErrorMessage = "Резюме с указанным идентификатором не найдено!";
        when(candidateService.deleteById(candidateId)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, candidateId);
        var actualErrorMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }
}
