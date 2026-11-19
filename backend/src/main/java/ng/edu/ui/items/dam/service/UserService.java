package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.UserCreateDto;
import ng.edu.ui.items.dam.dto.UserResponseDto;
import ng.edu.ui.items.dam.entity.Role;
import ng.edu.ui.items.dam.entity.User;
import ng.edu.ui.items.dam.enums.ERole;
import ng.edu.ui.items.dam.exception.DuplicateResourceException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.RoleRepository;
import ng.edu.ui.items.dam.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Section 4.2.1: administrator disables users; history attribution preserved.
 */
@Service
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> all() {
        return users.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        if (users.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }
        if (users.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (users.existsByWorkIdentifier(dto.getWorkIdentifier())) {
            throw new DuplicateResourceException("Work identifier already exists: " + dto.getWorkIdentifier());
        }
        Set<Role> rs = new HashSet<>();
        if (dto.getRoles() != null) {
            for (String r : dto.getRoles()) {
                ERole er = ERole.valueOf(r);
                rs.add(roles.findByName(er)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + r)));
            }
        }
        User u = User.builder()
                .name(dto.getName()).username(dto.getUsername())
                .workIdentifier(dto.getWorkIdentifier()).email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .unitName(dto.getUnitName()).enabled(true).roles(rs).build();
        return toDto(users.save(u));
    }

    @Transactional
    public UserResponseDto setEnabled(Long id, boolean enabled) {
        User u = users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        u.setEnabled(enabled);
        return toDto(u);
    }

    private UserResponseDto toDto(User u) {
        return UserResponseDto.builder()
                .id(u.getId()).name(u.getName()).username(u.getUsername())
                .workIdentifier(u.getWorkIdentifier()).email(u.getEmail())
                .unitName(u.getUnitName()).enabled(u.isEnabled())
                .roles(u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }
}
