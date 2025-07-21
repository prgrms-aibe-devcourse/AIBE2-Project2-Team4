package com.example.portpilot.global.config;

import com.example.portpilot.domain.user.UserService;
import com.example.portpilot.adminPage.admin.AdminService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final AdminService adminService;

    public SecurityConfig(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 사용자 로그인 설정
        http.formLogin()
                .loginPage("/users/login")
                .defaultSuccessUrl("/", true)
                .usernameParameter("email")
                .failureUrl("/users/login/error")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout"))
                .logoutSuccessUrl("/");

        // 어드민 로그인 설정
        http.formLogin()
                .loginPage("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .usernameParameter("email")
                .failureUrl("/admin/login/error")
                .loginProcessingUrl("/admin/login")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                .logoutSuccessUrl("/admin/login");

        // 접근 권한 설정 (순서 중요!)
        http.authorizeRequests()
                .mvcMatchers("/", "/users/**", "/item/**", "/images/**").permitAll()
                .mvcMatchers("/admin/login", "/admin/login/error").permitAll()
                .mvcMatchers("/admin/new").hasRole("ADMIN")
                .mvcMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
        auth.userDetailsService(adminService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }
}
