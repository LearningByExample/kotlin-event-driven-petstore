package org.learning.by.example.petstore.petqueries.routes

import org.learning.by.example.petstore.petqueries.model.Pet
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.kotlin.core.publisher.toMono

@Component
class PetRoutes {
    @Bean
    fun routes() = router {
        accept(APPLICATION_JSON).nest {
            "/pet".nest {
                GET("/{id}") {
                    ServerResponse.ok().body(Pet("fluffy", "dog", "german shepherd", "2020-06-28T00:00:00.0Z").toMono())
                }
            }
        }
    }
}
