package com.campus.incident.config;

import com.campus.incident.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private UserRepository userRepository;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/index.html").permitAll()
                
                // Incident creation - all authenticated users can create
                .requestMatchers("/api/incidents", "/api/incidents/simple").authenticated()
                
                // Incident viewing - all authenticated users can view
                .requestMatchers("/api/incidents/recent", "/api/incidents/urgent", "/api/incidents/overdue").authenticated()
                .requestMatchers("/api/incidents/dashboard/**").authenticated()
                .requestMatchers("/api/incidents/user-info").authenticated()
                
                // Incident updates - only MAINTENANCE and ADMIN
                .requestMatchers("/api/incidents/*/status", "/api/incidents/*/assign", "/api/incidents/*/start-work", 
                                "/api/incidents/*/pause-work", "/api/incidents/*/complete-work", "/api/incidents/*/close").hasAnyRole("MAINTENANCE", "ADMIN")
                
                // Incident deletion - only ADMIN
                .requestMatchers("/api/incidents/*").hasRole("ADMIN")
                
                // All other incident endpoints - require authentication
                .requestMatchers("/api/incidents/**").authenticated()
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Maintenance endpoints - require MAINTENANCE or ADMIN role
                .requestMatchers("/api/maintenance/**").hasAnyRole("MAINTENANCE", "ADMIN")
                
                // Default to requiring authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {})
            .userDetailsService(userDetailsService())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For H2 console
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return userRepository.findByUsername(username)
                    .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                        .disabled(!user.isActive())
                        .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            }
        };
    }
}
