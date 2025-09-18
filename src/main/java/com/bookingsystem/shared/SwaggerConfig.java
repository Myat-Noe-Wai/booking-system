package com.bookingsystem.shared;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Booking System Service", version = "1.0", description = "Booking System application"))
public class SwaggerConfig {

}
