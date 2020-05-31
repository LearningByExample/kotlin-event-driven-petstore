package org.learning.by.example.petstore.petcommands.handlers

import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.model.Result
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.net.URI
import java.util.*
import java.util.function.Consumer
import javax.validation.ConstraintViolation
import javax.validation.Validator


@Service
class PetHandler(val validator: Validator) {
    fun postPet(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono<Pet>().flatMap { pet ->
            val validate: Set<ConstraintViolation<Pet>> = validator.validate(pet)
            if (validate.isNotEmpty()) {
                var message = ""
                validate.forEach(Consumer {
                    message += "invalid ${it.propertyPath}, ${it.message}. "
                })
                message = message.trim()
                ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ErrorResponse("invalid pet", message).toMono())
            } else {
                with(Result(UUID.randomUUID().toString())) {
                    ServerResponse.created(URI.create("/pet/${id}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(this.toMono())
                }
            }
        }

    }
}
