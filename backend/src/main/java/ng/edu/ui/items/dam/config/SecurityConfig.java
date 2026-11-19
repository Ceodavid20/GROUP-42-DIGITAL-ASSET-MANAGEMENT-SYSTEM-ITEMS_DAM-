package ng.edu.ui.items.dam.config;

import ng.edu.ui.items.dam.security.CustomUserDetailsService;
import ng.edu.ui.items.dam.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Section 3.6 / Table 3.2: server-enforced permissions. UI hiding is not a control.
 * Stateless, HTTPS assumed at reverse proxy, BCrypt adaptive hashing.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:80"));
        c.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // NOTE: server.servlet.context-path (/api/v1) is stripped before
                // security matching, so matchers below omit the /api/v1 prefix.
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/assets/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "UNIT_CUSTODIAN", "MAINTENANCE_TECHNICIAN", "AUDITOR")
                .requestMatchers("/assets/**").hasAnyRole("ADMIN", "ASSET_OFFICER")
                .requestMatchers("/assignments/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "AUDITOR")
                .requestMatchers("/transfers/*/decision").hasAnyRole("ADMIN", "ASSET_OFFICER")
                .requestMatchers("/transfers/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "UNIT_CUSTODIAN", "AUDITOR")
                .requestMatchers("/maintenance/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "UNIT_CUSTODIAN", "MAINTENANCE_TECHNICIAN", "AUDITOR")
                .requestMatchers("/attachments/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "AUDITOR")
                .requestMatchers("/reports/summary/unit").hasAnyRole("ADMIN", "ASSET_OFFICER", "UNIT_CUSTODIAN", "AUDITOR")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "AUDITOR")
                .requestMatchers("/audit-events/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "AUDITOR")
                .requestMatchers("/reference/**").hasAnyRole("ADMIN", "ASSET_OFFICER", "UNIT_CUSTODIAN", "MAINTENANCE_TECHNICIAN", "AUDITOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(401);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
                .accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                }));
        return http.build();
    }
}
