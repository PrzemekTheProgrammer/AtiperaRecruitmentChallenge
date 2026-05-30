package pszerszenowicz;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
record GithubConfig(String baseUrl) {}
