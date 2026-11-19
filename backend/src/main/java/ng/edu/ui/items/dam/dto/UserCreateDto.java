package ng.edu.ui.items.dam.dto;

import lombok.Data;

@Data
public class UserCreateDto {
    private String name;
    private String username;
    private String workIdentifier;
    private String email;
    private String password;
    private String unitName;
    private java.util.Set<String> roles;
}
