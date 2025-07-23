package com.example.portpilot.global.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager userAuthManager;
    private final AuthenticationManager adminAuthManager;

    public CustomAuthenticationFilter(AuthenticationManager userAuthManager,
                                      AuthenticationManager adminAuthManager) {
        super(userAuthManager);
        this.userAuthManager = userAuthManager;
        this.adminAuthManager = adminAuthManager;
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/admin")) {
            super.setAuthenticationManager(adminAuthManager);
        } else {
            super.setAuthenticationManager(userAuthManager);
        }

        return super.attemptAuthentication(request, response);
    }
}
