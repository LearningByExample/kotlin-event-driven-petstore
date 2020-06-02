package org.learning.by.example.petstore.petcommands.handlers

import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.utils.DTOHelper
import org.learning.by.example.petstore.petcommands.utils.InvalidDtoException
import org.learning.by.example.petstore.petcommands.utils.ServerConstants.Companion.INVALID_RESOURCE
import org.learning.by.example.petstore.petcommands.utils.ServerConstants.Companion.SERVER_ERROR
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
class PetHandler(val dto: DTOHelper) {
    private fun toResponse(pet: Pet) = with(Result(UUID.randomUUID().toString())) {
        ServerResponse.created(URI.create("/pet/${id}"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(this.toMono())
    }

    private fun toError(throwable: Throwable) = if (throwable is InvalidDtoException) {
        ServerResponse.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse(INVALID_RESOURCE, throwable.message!!).toMono())
    } else {
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse(SERVER_ERROR, throwable.localizedMessage!!).toMono())
    }

    private fun validate(monoPet: Mono<Pet>) = dto.validate(monoPet)

    fun postPet(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono<Pet>()
            .transform(this::validate)
            .flatMap(this::toResponse)
            .onErrorResume(this::toError)
    }
}
