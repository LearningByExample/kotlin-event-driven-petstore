package org.learning.by.example.petstore.petcommands.service

import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.learning.by.example.petstore.petcommands.model.Pet
import org.springframework.stereotype.Service

@Service
class PetCommandsImpl(private val commandsProducer: CommandsProducer) : PetCommands {
    companion object {
        const val CREATE_PET_COMMAND = "pet_create"
    }

    override fun sendPetCreate(pet: Pet) = commandsProducer.sendCommand(
        command(CREATE_PET_COMMAND) {
            "name" value pet.name!!
            "category" value pet.category!!
            "tags" values pet.tags!!
        }
    ).onErrorMap(::SendPetCreateException)
}
