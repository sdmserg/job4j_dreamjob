package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Properties;

import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepository() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");
        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);
        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @Test
    public void whenSaveUserThenGetSame() {
        var user = sql2oUserRepository.save(new User("test@mail.com", "Ivan", "123")).get();
        var savedUser = sql2oUserRepository.findUserByEmailAndPassword("test@mail.com", "123").get();
        assertThat(savedUser).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenSaveTwoUsersWithSameEmailThenSecondFails() {
        var user1 = new User("test2@mail.com", "Ivan", "123");
        var user2 = new User("test2@mail.com", "Petr", "456");
        var savedUser1 = sql2oUserRepository.save(user1);
        var savedUser2 = sql2oUserRepository.save(user2);
        assertThat(savedUser1.isPresent()).isTrue();
        assertThat(savedUser2.isEmpty()).isTrue();
    }

    @Test
    public void whenFindByEmailDoesntExistsThenNothingFound() {
        assertThat(sql2oUserRepository.findUserByEmailAndPassword("test@yandex.ru", "123")).isEqualTo(empty());
    }
}