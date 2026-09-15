package com.hackathon.spotify.ratelimit;

import com.hackathon.spotify.model.Plan;
import com.hackathon.spotify.ratelimit.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimiterContractFilter
                extends OncePerRequestFilter {

        private final RateLimiterService rateLimiterService;

        public RateLimiterContractFilter(
                        RateLimiterService rateLimiterService) {

                this.rateLimiterService = rateLimiterService;
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain chain)
                        throws ServletException, IOException {

                String endpoint = getEndpoint(request);

                /*
                 * If this endpoint is not part of the
                 * rate-limiting rules, continue normally.
                 */
                if (endpoint.equals("UNKNOWN")) {
                        chain.doFilter(request, response);
                        return;
                }

                String userId;
                Plan plan;

                /*
                 * LOGIN happens before JWT authentication.
                 *
                 * Therefore we cannot get userId/plan
                 * from AuthFilter.
                 *
                 * Use client IP for login.
                 */
                if (endpoint.equals("LOGIN")) {

                        userId = getClientIp(request);

                        /*
                         * User plan is unknown before login.
                         *
                         * Therefore use FREE login limit.
                         */
                        plan = Plan.FREE;

                } else {

                        /*
                         * AuthFilter has already executed before
                         * this filter.
                         *
                         * AuthFilter puts the actual userId and
                         * plan into the request.
                         */
                        Object userAttribute = request.getAttribute(
                                        RateLimitRequestContext.USER_ID);

                        Object planAttribute = request.getAttribute(
                                        RateLimitRequestContext.PLAN);

                        /*
                         * If there is no authenticated user,
                         * allow Spring Security to handle authentication.
                         */
                        if (userAttribute == null ||
                                        planAttribute == null) {

                                chain.doFilter(request, response);
                                return;
                        }

                        userId = userAttribute.toString();

                        plan = (Plan) planAttribute;
                }

                /*
                 * Call the actual rate limiter service.
                 */
                RateLimitResult result = rateLimiterService.checkLimit(
                                userId,
                                endpoint,
                                plan);

                /*
                 * Always send rate-limit information.
                 */
                response.setHeader(
                                "X-Ratelimit-Limit",
                                String.valueOf(result.getLimit()));

                response.setHeader(
                                "X-Ratelimit-Remaining",
                                String.valueOf(result.getRemaining()));

                response.setHeader(
                                "Access-Control-Expose-Headers",
                                "X-Ratelimit-Limit, " +
                                                "X-Ratelimit-Remaining, " +
                                                "X-Ratelimit-Retry-After");

                /*
                 * Rate limit exceeded.
                 */
                if (!result.isAllowed()) {

                        response.setStatus(429);

                        response.setHeader(
                                        "X-Ratelimit-Retry-After",
                                        String.valueOf(
                                                        result.getRetryAfter()));

                        response.setHeader(
                                        "Retry-After",
                                        String.valueOf(
                                                        result.getRetryAfter()));

                        response.setContentType(
                                        "text/plain");

                        response.getWriter().write(
                                        "Too many requests, please try again later.");

                        return;
                }

                /*
                 * Request is allowed.
                 *
                 * Continue to the next filter/controller.
                 */
                chain.doFilter(request, response);
        }

        /**
         * Converts the API path into the endpoint
         * understood by RateLimiterService.
         */
        private String getEndpoint(
                        HttpServletRequest request) {

                String path = request.getRequestURI();

                if (path.equals("/api/auth/login")) {
                        return "LOGIN";
                }

                if (path.equals("/api/search")) {
                        return "SEARCH";
                }

                if (path.equals("/api/play")) {
                        return "PLAY";
                }

                if (path.equals("/api/playlists") ||
                                path.equals("/api/playlists/")) {

                        return "PLAYLIST";
                }

                if (path.equals("/api/profile") ||
                                path.equals("/api/history") ||
                                path.equals("/api/songs")) {

                        return "OTHER";
                }

                return "UNKNOWN";
        }

        /**
         * Gets the client's IP address.
         *
         * X-Forwarded-For is checked first because
         * the application may eventually run behind
         * a proxy/load balancer.
         */
        private String getClientIp(
                        HttpServletRequest request) {

                String forwarded = request.getHeader("X-Forwarded-For");

                if (forwarded != null &&
                                !forwarded.isEmpty()) {

                        return forwarded
                                        .split(",")[0]
                                        .trim();
                }

                return request.getRemoteAddr();
        }
}