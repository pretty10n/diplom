package jd.ru.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coreFormOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Core Form Service API")
                        .version("v1")
                        .description("CRUD API для форм 4/5/6 (MVP)"))
                .servers(List.of(new Server().url("/").description("Default server")));
    }
}
