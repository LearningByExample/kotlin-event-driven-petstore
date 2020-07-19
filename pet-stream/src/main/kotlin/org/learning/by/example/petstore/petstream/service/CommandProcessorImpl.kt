package org.learning.by.example.petstore.petstream.service

import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.petstream.model.Pet
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.into
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommandProcessorImpl(val databaseClient: DatabaseClient) : CommandProcessor {
    override fun process(command: Mono<Command>): Mono<Void> {
        return command.flatMap {
            databaseClient
                .insert().into<Pet>()
                .using(Pet(it.id.toString(), it.get("name"), it.get("dob")))
                .then()
        }
    }
}
