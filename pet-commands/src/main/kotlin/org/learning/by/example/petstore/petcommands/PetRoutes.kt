package org.learning.by.example.petstore.petcommands

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class PetRoutes {
    @Bean
    fun routes() = router {
        accept(APPLICATION_JSON).nest {
            contentType(APPLICATION_JSON).nest {
                "/pet".nest {
                    POST("/") {
                        ok().body(it.bodyToMono(String::class.java), String::class.java)
                    }
                }
            }
        }
    }
}
