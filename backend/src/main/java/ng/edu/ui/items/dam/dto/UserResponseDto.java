package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String username;
    private String workIdentifier;
    private String email;
    private String unitName;
    private boolean enabled;
    private Set<String> roles;
}
