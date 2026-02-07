package com.microlift.securityconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthFilter;

        @Autowired
        private AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                // OPTIONS requests MUST BE FIRST for CORS preflight
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()

                                                // Specific ADMIN-only auth endpoints BEFORE general /api/auth/**
                                                .requestMatchers("/api/auth/verify-user/**", "/api/auth/kyc-files/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // General public auth endpoints (login, register)
                                                .requestMatchers("/api/auth/**", "/api/campaigns/public/**")
                                                .permitAll()

                                                .requestMatchers("/api/auth/users/**").authenticated()
                                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(org.springframework.http.HttpMethod.DELETE,
                                                                "/api/admin/users/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/api/donor/**")
                                                .hasAnyAuthority("ROLE_DONOR", "ROLE_ADMIN")
                                                .requestMatchers("/api/beneficiary/**")
                                                .hasAnyAuthority("ROLE_BENEFICIARY", "ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers("/v3/api-docs/**", "/swagger-ui/**");
        }
}
