package org.operaton.dev.starter.server.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * Forwards unknown non-API, non-asset requests to index.html so Vue Router
 * can handle client-side routes (e.g. /configure, /gallery).
 */
@Configuration
public class SpaWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpaFallbackInterceptor());
    }

    static class SpaFallbackInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            String path = request.getRequestURI();
            // Let API calls, static assets, and actuator pass through unchanged
            if (path.startsWith("/api/") || path.startsWith("/actuator/")
                    || path.contains(".") || path.equals("/")) {
                return true;
            }
            // Forward SPA routes to index.html
            request.getRequestDispatcher("/index.html").forward(request, response);
            return false;
        }
    }
}
