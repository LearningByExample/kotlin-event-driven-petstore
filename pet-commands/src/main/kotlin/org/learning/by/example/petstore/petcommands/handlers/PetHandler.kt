package org.learning.by.example.petstore.petcommands.handlers

import org.learning.by.example.petstore.petcommands.model.Result
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.toMono
import java.net.URI
import java.util.*

@Service
class PetHandler {
    fun postPet(serverRequest: ServerRequest) = with(Result(UUID.randomUUID().toString())) {
        ServerResponse.created(URI.create("/pet/${id}")).body(this.toMono())
    }
}
