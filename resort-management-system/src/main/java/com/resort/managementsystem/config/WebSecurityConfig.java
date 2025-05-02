package com.resort.managementsystem.config;

import com.resort.managementsystem.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(CustomUserDetailsService uds) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        // Public access
                        .requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/admin/**", "/settings/**", "/audit/**").hasRole("ADMIN")
                        // Staff and Admin access
                        .requestMatchers("/staff/**", "/web/reservations/**", "/web/rooms/**", "/web/guests/**", "/web/maintenance-schedules/**").hasAnyRole("ADMIN", "STAFF")
                        // Account page for all authenticated users
                        .requestMatchers("/account").authenticated()
                        // Dashboard and other web endpoints
                        .requestMatchers("/web/**").authenticated()
                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/web/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }
}