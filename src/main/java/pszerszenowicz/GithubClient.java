package pszerszenowicz;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import pszerszenowicz.model.client.GithubBranch;
import pszerszenowicz.model.client.GithubRepo;

import java.util.List;

@Component
class GithubClient {

    private final RestClient restClient;

    // Pobieramy url z konfiguracji, aby łatwo podmienić go na WireMocka w testach
    GithubClient(RestClient.Builder restClientBuilder, GithubConfig config) {
        this.restClient = restClientBuilder.baseUrl(config.baseUrl()).build();
    }

    List<GithubRepo> fetchRepositories(String username) {
        try {
            return restClient.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(404)) {
                throw new UserNotFoundException("User with username '" + username + "' does not exist on GitHub");
            }
            throw e;
        }
    }

    List<GithubBranch> fetchBranches(String username, String repoName) {
        return restClient.get()
                .uri("/repos/{username}/{repo}/branches", username, repoName)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}