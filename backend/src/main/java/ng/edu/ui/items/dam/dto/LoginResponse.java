package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String name;
    private Set<String> roles;
}
