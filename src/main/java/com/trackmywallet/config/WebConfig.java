package com.trackmywallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Spring MVC configuration class.
 *
 * @Configuration tells Spring this class provides bean configuration.
 * WebMvcConfigurer lets us customize Spring's built-in web settings.
 *
 * Here we:
 *   1. Register the SessionInterceptor to run before every /api/** request
 *   2. Configure static resource serving for our HTML/CSS/JS frontend files
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionInterceptor sessionInterceptor;

    public WebConfig(SessionInterceptor sessionInterceptor) {
        this.sessionInterceptor = sessionInterceptor;
    }

    /**
     * Register the SessionInterceptor to intercept all /api/** requests.
     * The interceptor checks for a valid session before any API call reaches the controller.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/**");
    }

    /**
     * Serve static files (HTML, CSS, JS) from the /static/ folder.
     * When the browser requests "/", Spring will serve static/index.html.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
