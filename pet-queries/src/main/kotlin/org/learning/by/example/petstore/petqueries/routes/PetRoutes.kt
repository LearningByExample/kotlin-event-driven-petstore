package org.learning.by.example.petstore.petqueries.routes

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Component
class PetRoutes {
    @Bean
    fun routes() = router {
        accept(APPLICATION_JSON).nest {
            "/pet".nest {
                GET("/{id}") {
                    ServerResponse.ok().build()
                }
            }
        }
    }
}
