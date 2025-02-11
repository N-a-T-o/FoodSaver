package com.foodsaver.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SwaggerConfig.class)
class SwaggerConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void testOpenAPIBeanCreation() {
        assertNotNull(openAPI, "OpenAPI bean should not be null");

        Info info = openAPI.getInfo();
        assertNotNull(info, "Info object should not be null");
        assertEquals("FoodSaver API", info.getTitle(), "API title should match");
        assertEquals("REST API for Food Saver.", info.getDescription(), "API description should match");

        assertTrue(openAPI.getSecurity().stream()
                        .anyMatch(securityRequirement -> securityRequirement.containsKey("Bearer Authentication")),
                "SecurityRequirement should include 'Bearer Authentication'");

        Components components = openAPI.getComponents();
        assertNotNull(components, "Components object should not be null");
        SecurityScheme securityScheme = components.getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme, "SecurityScheme 'Bearer Authentication' should be defined");

        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType(), "SecurityScheme type should be HTTP");
        assertEquals("JWT", securityScheme.getBearerFormat(), "Bearer format should be JWT");
        assertEquals("bearer", securityScheme.getScheme(), "Scheme should be 'bearer'");
    }
}
