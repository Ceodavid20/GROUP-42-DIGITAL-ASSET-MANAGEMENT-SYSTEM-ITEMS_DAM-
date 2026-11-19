package ng.edu.ui.items.dam.controller;

import jakarta.validation.Valid;
import ng.edu.ui.items.dam.dto.LoginRequest;
import ng.edu.ui.items.dam.dto.LoginResponse;
import ng.edu.ui.items.dam.security.CustomUserDetailsService;
import ng.edu.ui.items.dam.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * FR-01: invalid credentials -> 401; valid -> signed token session.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final CustomUserDetailsService users;
    private final JwtUtil jwt;

    public AuthController(AuthenticationManager authManager, CustomUserDetailsService users, JwtUtil jwt) {
        this.authManager = authManager;
        this.users = users;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetails ud = (UserDetails) auth.getPrincipal();
        String token = jwt.generateToken(ud);
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .username(ud.getUsername())
                .name(ud.getUsername())
                .roles(ud.getAuthorities().stream().map(Object::toString).collect(Collectors.toSet()))
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(Authentication auth) {
        UserDetails ud = users.loadUserByUsername(auth.getName());
        return ResponseEntity.ok(LoginResponse.builder()
                .username(ud.getUsername())
                .name(ud.getUsername())
                .roles(ud.getAuthorities().stream().map(Object::toString).collect(Collectors.toSet()))
                .build());
    }
}
