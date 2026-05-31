package pszerszenowicz;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pszerszenowicz.model.dto.ErrorResponseDto;
import pszerszenowicz.model.dto.UserResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
public class AppController {
    private final AppService appService;

    AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/{username}")
    ResponseEntity<List<UserResponseDto>> getRepositories(@PathVariable String username) {
        return ResponseEntity.ok(appService.getUserRepositories(username));
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
