package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserService userService;

    private UserController userController;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    public void whenRequestUserRegisterPageThenGetRegisterPage() {
        var view = userController.getRegistrationPage();
        assertThat(view).isEqualTo("users/register");
    }

    @Test
    public void whenRegisterUserSuccessfullyThenRedirectToUserLoginPage() {
        var user = new User("test@gmail.com", "username", "password");
        when(userService.save(any(User.class))).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);

        assertThat(view).isEqualTo("redirect:/users/login");
    }

    @Test
    public void whenRegisterUserFailedThenGetErrorPageWithMessage() {
        var user = new User("test@gmail.com", "username", "password");
        var expectedErrorMessage = "Пользователь с таким email уже существует!";
        when(userService.save(any(User.class))).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var actualErrorMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    public void whenRegisterUserThrowsExceptionThenGetErrorPageWithMessage() {
        var user = new User("test@gmail.com", "username", "password");
        var expectedException = new RuntimeException("Внутренняя ошибка сервиса!");
        when(userService.save(any(User.class))).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestUserLoginPageThenGetLoginPage() {
        var view = userController.getLoginPage();
        assertThat(view).isEqualTo("users/login");
    }

    @Test
    public void whenLoginUserSuccessfullyThenRedirectToVacancyPage() {
        var user = new User("test@gmail.com", "username", "password");
        var request = mock(HttpServletRequest.class);
        var session = mock(HttpSession.class);
        when(userService.findUserByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.of(user));
        when(request.getSession()).thenReturn(session);

        var model = new ConcurrentModel();
        var view = userController.loginUser(model, user, request);

        assertThat(view).isEqualTo("redirect:/vacancies/list");
        verify(session).setAttribute("user", user);
    }

    @Test
    public void whenLoginUserFailedThenGetErrorPageWithMessage() {
        var user = new User("test@gmail.com", "username", "password");
        var request = mock(HttpServletRequest.class);
        var expectedErrorMessage = "Почта или пароль введены неверно!";
        when(userService.findUserByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.loginUser(model, user, request);
        var actualErrorMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    public void whenUserLogoutThenIvalidateUserSessionAndRedirectToUserLoginPage() {
        var session = mock(HttpSession.class);

        var view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/users/login");
        verify(session).invalidate();
    }
}