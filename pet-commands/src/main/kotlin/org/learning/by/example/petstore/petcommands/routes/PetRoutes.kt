package org.learning.by.example.petstore.petcommands.routes

import org.learning.by.example.petstore.petcommands.handlers.PetHandler
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class PetRoutes(val petHandler: PetHandler) {
    @Bean
    fun routes() = router {
        accept(APPLICATION_JSON).nest {
            contentType(APPLICATION_JSON).nest {
                "/pet".nest {
                    POST("/", petHandler::postPet)
                }
            }
        }
    }
}
