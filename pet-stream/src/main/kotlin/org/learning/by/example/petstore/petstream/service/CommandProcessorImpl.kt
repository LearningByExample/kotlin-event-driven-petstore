package org.learning.by.example.petstore.petstream.service

import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.petstream.model.Pet
import org.learning.by.example.petstore.petstream.respository.PetRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommandProcessorImpl(val petRepository: PetRepository) : CommandProcessor {
    override fun process(command: Mono<Command>) = command.flatMap {
        petRepository.save(Pet(it.id.toString(), it.commandName, it.timestamp)).flatMap {
            Mono.empty<Void>()
        }
    }
}
