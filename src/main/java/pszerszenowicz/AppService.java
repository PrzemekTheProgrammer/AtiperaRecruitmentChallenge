package pszerszenowicz;

import org.springframework.stereotype.Service;
import pszerszenowicz.model.client.GithubBranch;
import pszerszenowicz.model.client.GithubRepo;
import pszerszenowicz.model.dto.BranchDto;
import pszerszenowicz.model.dto.UserResponseDto;

import java.util.List;

@Service
public class AppService {
    private final GithubClient githubClient;

    AppService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<UserResponseDto> getUserRepositories(String username) {
        List<GithubRepo> repos = githubClient.fetchRepositories(username);

        return repos.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> {
                    List<GithubBranch> githubBranches = githubClient.fetchBranches(username, repo.name());
                    List<BranchDto> branchDtos = githubBranches.stream()
                            .map(b -> new BranchDto(b.name(), b.commit().sha()))
                            .toList();
                    return new UserResponseDto(repo.name(), repo.owner().login(), branchDtos);
                })
                .toList();
    }
}