package echangelocal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String UPLOAD_PATH_PATTERN = "/uploads/**";
    private static final String UPLOAD_LOCATION = "file:./uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(UPLOAD_PATH_PATTERN)
                .addResourceLocations(UPLOAD_LOCATION);
    }
}