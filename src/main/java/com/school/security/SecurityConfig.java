package com.school.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Form-login security. Passwords are the existing jBCrypt ({@code $2a$}) hashes,
 * which {@link BCryptPasswordEncoder} verifies natively. CSRF protection is left
 * on (Spring default) so every Thymeleaf POST form is protected.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/login").permitAll()
                // Admin-only areas — enforced at the URL as well as in the controllers.
                .requestMatchers("/users/**", "/announcements/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            // Frames disabled is fine; keep default security headers.
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable()));
        return http.build();
    }
}
