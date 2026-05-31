package pszerszenowicz;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pszerszenowicz.model.dto.UserResponseDto;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    static void teardown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.baseUrl", () -> "http://localhost:8089");
    }

    @Test
    void shouldReturnUserRepositoriesSuccessfully() {
        // Given
        String username = "testuser";

        stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "challenge-repo",
                                    "fork": false,
                                    "owner": { "login": "testuser" }
                                }
                            ]
                            """)));

        stubFor(get(urlEqualTo("/repos/" + username + "/challenge-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "main",
                                    "commit": { "sha": "abcdef1234567890" }
                                }
                            ]
                            """)));

        // When
        ResponseEntity<List<UserResponseDto>> response = restTemplate.exchange(
                "/api/repositories/" + username,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<UserResponseDto> repositories = response.getBody();
        assertThat(repositories).hasSize(1);

        UserResponseDto repo = repositories.get(0);
        assertThat(repo.repositoryName()).isEqualTo("challenge-repo");
        assertThat(repo.ownerLogin()).isEqualTo("testuser");
        assertThat(repo.branches()).hasSize(1);
        assertThat(repo.branches().get(0).name()).isEqualTo("main");
        assertThat(repo.branches().get(0).lastCommitSha()).isEqualTo("abcdef1234567890");
    }

    @Test
    void shouldReturn404WhenUserNotFoundInGithub() {
        // Given
        String unknownUser = "nonexistentuser";

        stubFor(get(urlEqualTo("/users/" + unknownUser + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/users/" + unknownUser,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
