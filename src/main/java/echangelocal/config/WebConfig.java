package echangelocal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // S'assurer que le chemin se termine par un "/"
        String uploadLocation = "file:" + uploadDir + (uploadDir.endsWith("/") ? "" : "/");

        // Enregistrer le handler pour servir les fichiers depuis le dossier uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);

        // Ajouter aussi le classpath pour les ressources statiques
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}