package org.example.assignment2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    @NonNull
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**", "/users/**", "/settings/**", "/dashboard", "/posts/**")
                .excludePathPatterns(
                        "/login", 
                        "/logout", 
                        "/forgot-password", 
                        "/send-otp", 
                        "/verify-otp", 
                        "/reset-password",
                        "/users/register",
                        "/css/**", 
                        "/js/**", 
                        "/images/**", 
                        "/uploads/**"
                );
    }
}
