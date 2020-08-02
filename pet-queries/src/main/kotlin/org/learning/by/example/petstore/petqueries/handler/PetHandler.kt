package org.learning.by.example.petstore.petqueries.handler

import org.learning.by.example.petstore.petqueries.service.PetService
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.util.UUID

@Service
class PetHandler(val petService: PetService) {
    fun getPet(serverRequest: ServerRequest) =
        with(UUID.fromString(serverRequest.pathVariable("id"))) {
            petService.findPetById(this)
                .flatMap {
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(it.toMono())
                }
                .switchIfEmpty {
                    ServerResponse.notFound().build()
                }
        }
}
