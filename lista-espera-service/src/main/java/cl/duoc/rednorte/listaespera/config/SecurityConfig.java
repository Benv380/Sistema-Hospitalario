package cl.duoc.rednorte.listaespera.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity // Habilita explícitamente la seguridad web
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // 1. Deshabilitar CSRF (necesario para APIs REST con JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Configurar CORS usando el bean definido abajo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. Stateless: No manejamos sesiones en el servidor
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Autorización de rutas
            .authorizeHttpRequests(auth -> auth
                // Permitir todas las opciones (Preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Permitir Login y Registro sin Token
                .requestMatchers("/api/auth/**").permitAll()
                
                // Cualquier otra ruta de la API requiere estar logueado
                .requestMatchers("/api/v1/**").authenticated()
                
                // Bloqueo total por defecto para cualquier otra cosa
                .anyRequest().authenticated()
            )
            
            // 5. Inyectar el filtro de JWT antes del filtro de usuario/password estándar
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // El origen de tu Frontend (Vite)
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // Métodos permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers permitidos (el * permite Authorization, Content-Type, etc.)
        config.setAllowedHeaders(List.of("*"));
        
        // Permitir envío de cookies/auth headers
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usamos BCrypt para las contraseñas
        return new BCryptPasswordEncoder();
    }
}