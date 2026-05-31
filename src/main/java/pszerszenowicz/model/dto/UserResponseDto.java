package pszerszenowicz.model.dto;

import java.util.List;

public record UserResponseDto(String repositoryName, String ownerLogin, List<BranchDto> branches) {
}
