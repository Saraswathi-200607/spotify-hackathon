package com.hackathon.spotify.security;

//import com.hackathon.spotify.model.User;
import com.hackathon.spotify.repository.UserRepository;
import com.hackathon.spotify.ratelimit.RateLimitRequestContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository users;

    public AuthFilter(
            JwtService jwtService,
            UserRepository users) {

        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            if (jwtService.valid(token)) {

                String userId = jwtService.extractUserId(token);

                users.findByUserId(userId).ifPresent(user -> {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUserId(),
                                    null,
                                    null
                            );

                    auth.setDetails(user);

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(auth);

                    // Send actual user ID to Rate Limiter
                    request.setAttribute(
                            RateLimitRequestContext.USER_ID,
                            user.getUserId()
                    );

                    // Send actual user plan to Rate Limiter
                    request.setAttribute(
                            RateLimitRequestContext.PLAN,
                            user.getPlan()
                    );
                });
            }
        }

        chain.doFilter(request, response);
    }
}