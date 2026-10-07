package org.example.studentmanagementsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Exposes the uploaded student images over HTTP: a file saved as uploads/student-1.jpg
 * becomes reachable at /images/student-1.jpg (the imageUrl in the student response).
 * The URL is behind the JWT filter chain, so send the Bearer token.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
    }
}