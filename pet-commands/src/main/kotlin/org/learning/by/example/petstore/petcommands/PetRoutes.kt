package org.learning.by.example.petstore.petcommands

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class PetRoutes {
    @Bean
    fun routes() = router {
        accept(APPLICATION_JSON_UTF8).nest {
            contentType(APPLICATION_JSON_UTF8).nest {
                "/pet".nest {
                    POST("/") {
                        ok().build()
                    }
                }
            }
        }
    }
}
