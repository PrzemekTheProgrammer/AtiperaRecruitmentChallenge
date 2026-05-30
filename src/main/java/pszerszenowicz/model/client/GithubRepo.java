package pszerszenowicz.model.client;

public record GithubRepo(String name, GithubOwner owner, boolean fork) {
}
