package ng.edu.ui.items.dam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI damOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("UI ITeMS Digital Asset Management API")
                        .version("1.0.0")
                        .description("ICT equipment lifecycle records for ITeMS, University of Ibadan. "
                                + "Design-stage prototype with fictitious DEMO data."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
