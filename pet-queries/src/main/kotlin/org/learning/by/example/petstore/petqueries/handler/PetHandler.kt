package org.learning.by.example.petstore.petqueries.handler

import org.learning.by.example.petstore.petqueries.model.Pet
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.kotlin.core.publisher.toMono

@Service
class PetHandler {
    fun getPet(serverRequest: ServerRequest) = ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(Pet("fluffy", "dog", "german shepherd", "2020-06-28T00:00:00.0Z").toMono())
}
