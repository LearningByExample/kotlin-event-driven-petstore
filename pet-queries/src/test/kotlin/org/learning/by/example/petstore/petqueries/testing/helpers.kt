package org.learning.by.example.petstore.petqueries.testing

import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

inline fun <reified Type> Mono<ServerResponse>.verify(crossinline verifyFun: (ServerResponse, Type) -> Unit) {
    StepVerifier.create(this)
        .consumeNextWith { response ->
            if (response is EntityResponse<*>) {
                val entity = response.entity()
                if (entity is Mono<*>) {
                    StepVerifier.create(entity)
                        .consumeNextWith { body ->
                            if (body is Type) {
                                verifyFun(response, body)
                            } else {
                                throw AssertionError("response has not a valid object type, is ${body.javaClass}")
                            }
                        }.verifyComplete()
                } else {
                    throw AssertionError("response has not a mono")
                }
            } else {
                throw AssertionError("response was not an entity")
            }
        }
        .verifyComplete()
}
