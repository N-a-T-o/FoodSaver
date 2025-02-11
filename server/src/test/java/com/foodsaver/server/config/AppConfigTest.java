package com.foodsaver.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.foodsaver.server.authorization.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = AppConfig.class)
class AppConfigTest {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter messageConverter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    void testModelMapperBean() {
        assertNotNull(modelMapper, "ModelMapper bean should not be null");
    }

    @Test
    void testMappingJackson2HttpMessageConverterBean() {
        assertNotNull(messageConverter, "MappingJackson2HttpMessageConverter bean should not be null");

        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        assertTrue(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) == false,
                "SerializationFeature.WRITE_DATES_AS_TIMESTAMPS should be disabled");
    }

    @Test
    void testJwtAuthenticationEntryPointBean() {
        assertNotNull(jwtAuthenticationEntryPoint, "JwtAuthenticationEntryPoint bean should not be null");
    }
}
