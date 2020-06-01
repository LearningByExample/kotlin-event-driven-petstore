package org.learning.by.example.petstore.petcommands.handlers

import org.learning.by.example.petstore.petcommands.exceptions.InvalidParametersException
import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.utility.Utils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import java.util.*


@Service
class PetHandler(val utils: Utils) {
    fun postPet(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono<Pet>()
            .transform { utils.validate(it) }
            .flatMap {
                with(Result(UUID.randomUUID().toString())) {
                    ServerResponse.created(URI.create("/pet/${id}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(this.toMono())
                }

            }.onErrorResume {
                if( it is InvalidParametersException) {
                    ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ErrorResponse("Invalid pet", it.message!!).toMono())
                } else {
                    ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                        .body(ErrorResponse("Server Error", it.localizedMessage!!).toMono())
                }

            }
    }
}
