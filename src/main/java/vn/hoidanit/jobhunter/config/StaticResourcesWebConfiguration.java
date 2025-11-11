package vn.hoidanit.jobhunter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourcesWebConfiguration
        implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình để truy cập file từ thư mục public trong project
        // URL: /storage/** sẽ map tới thư mục public/
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:public/");
    }
}
