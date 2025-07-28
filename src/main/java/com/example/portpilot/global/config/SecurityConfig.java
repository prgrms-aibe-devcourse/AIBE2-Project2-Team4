package com.example.portpilot.global.config;

import com.example.portpilot.adminPage.admin.AdminService;
import com.example.portpilot.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AdminService adminService;

    @Bean
    public AuthenticationManager userAuthManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public AuthenticationManager adminAuthManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @Primary
    public AuthenticationManager defaultAuthManager() {
        return userAuthManager();
    }

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customFilter =
                new CustomAuthenticationFilter(userAuthManager(), adminAuthManager());
        customFilter.setFilterProcessesUrl("/admin/login");
        customFilter.setUsernameParameter("email");
        customFilter.setPasswordParameter("password");
        customFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.sendRedirect("/admin/login/error");
        });
        customFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.sendRedirect("/admin");
        });

        http
                .antMatcher("/admin/**")
                .authorizeRequests(authz -> authz
                        .antMatchers(
                                "/admin/login",
                                "/admin/login/error",
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/admin/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/admin/login");
                        })
                )
                .addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin().disable()
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                        .logoutSuccessUrl("/admin/login")
                );

        return http.build();
    }


    @Bean
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/", "/users/login", "/users/login/error", "/item/**", "/images/**", "/img/**", "/users/new").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/users/login")
                .loginProcessingUrl("/users/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/users/login/error")
                .and()
                .logout()
                .logoutUrl("/users/logout")
                .logoutSuccessUrl("/users/login");

        return http.build();
    }

    @Bean
    @Order(0) // 가장 먼저 적용
    public SecurityFilterChain staticResources(HttpSecurity http) throws Exception {
        http
                .antMatcher("/css/**")
                .authorizeRequests()
                .antMatchers("/error/**").permitAll()
                .anyRequest().permitAll()
                .and()
                .requestCache().disable()
                .securityContext().disable()
                .sessionManagement().disable();

        return http.build();
    }

}
