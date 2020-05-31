package org.learning.by.example.petstore.petcommands.handlers

import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.model.Result
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.net.URI
import java.util.*


@Service
class PetHandler(val validator: Validator) {

    fun postPet(serverRequest: ServerRequest): Mono<ServerResponse> {
        val pet : Mono<Pet> = serverRequest.bodyToMono()
        return pet.flatMap {
            val errors: Errors = BeanPropertyBindingResult(pet, Pet::class.java.name)
            validator.validate(pet, errors)
            if (errors.allErrors.isNotEmpty()) {
                ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ErrorResponse("error!", "").toMono())
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
