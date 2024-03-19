/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Local Server URL"),
                @Server(url = "/api", description = "Integration Server URL")
        }
)
public class SpringDocConfig {

    @Bean
    public OpenAPI usersMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Your API Title")
                        .description("Your API Description")
                        .version("1.0"));
    }

   /* @Bean
    public OpenAPI myOpenAPI(final Environment environment) {
        final String oauth2Authentication = "Oauth2Authentication";
        return new OpenAPI()
                .security(List.of(
                                new SecurityRequirement()
                                        .addList(oauth2Authentication, environment.getProperty("SPRINGDOC_SWAGGERUI_OAUTH_API"))
                        )
                )
                .schemaRequirement(oauth2Authentication, new SecurityScheme()
                        .name(oauth2Authentication)
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows()
                                .implicit(new OAuthFlow()
                                        .authorizationUrl("https://login.microsoftonline.com/organizations/oauth2/v2.0/authorize")
                                        .scopes(new Scopes()
                                                .addString(environment.getProperty("SPRINGDOC_SWAGGERUI_OAUTH_API"), "Oauth2 Authentication")
                                        )
                                )
                        )
                )
                .schemaRequirement("BearerAuth", new SecurityScheme()
                        .name("BearerAuth")
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                );
    }*/

}
