package org.learning.by.example.petstore.petcommands.handlers

import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.service.PetCommands
import org.learning.by.example.petstore.reactor.dtovalidator.DTOValidator
import org.learning.by.example.petstore.reactor.dtovalidator.InvalidDtoException
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


@Service
class PetHandler(
    val dto: DTOValidator,
    val petCommands: PetCommands
) {
    companion object {
        const val INVALID_RESOURCE = "Invalid Resource"
        const val SERVER_ERROR = "Server Error"
    }

    private fun toResponse(id: String) = with(Result(id)) {
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

    fun postPet(serverRequest: ServerRequest) = serverRequest.bodyToMono<Pet>()
        .transform(this::validate)
        .transform(petCommands::sendPetCreate)
        .flatMap(this::toResponse)
        .onErrorResume(this::toError)
}
