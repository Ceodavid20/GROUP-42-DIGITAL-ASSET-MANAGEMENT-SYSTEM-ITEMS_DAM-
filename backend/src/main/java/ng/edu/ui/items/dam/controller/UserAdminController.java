package ng.edu.ui.items.dam.controller;

import ng.edu.ui.items.dam.dto.UserCreateDto;
import ng.edu.ui.items.dam.dto.UserResponseDto;
import ng.edu.ui.items.dam.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Table 3.2: only Admin manages user access.
 */
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService users;

    public UserAdminController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> all() {
        return ResponseEntity.ok(users.all());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> create(@RequestBody UserCreateDto dto) {
        UserResponseDto saved = users.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + saved.getId())).body(saved);
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponseDto> setEnabled(@PathVariable Long id,
                                                      @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(users.setEnabled(id, Boolean.TRUE.equals(body.get("enabled"))));
    }
}
