package com.onlineauction.OnlineAuction.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Online Auction",
                description = "Diplom project", version = "1.0.0",
                contact = @Contact(
                        name = "Marusik Matvey",
                        email = "matvey.marusik@mail.ru"
                )
        )
)
public class SwaggerConfig {

}
