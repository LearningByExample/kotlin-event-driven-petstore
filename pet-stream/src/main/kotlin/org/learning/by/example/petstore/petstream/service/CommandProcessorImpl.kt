package org.learning.by.example.petstore.petstream.service

import org.learning.by.example.petstore.command.Command
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service

@Service
class CommandProcessorImpl(val databaseClient: DatabaseClient) : CommandProcessor {
    override fun process(cmd: Command) = insertCategory(cmd.get("category")).flatMap { category ->
        insertBreed(cmd.get("breed")).flatMap { breed ->
            insertPet(cmd, category, breed)
        }
    }

    fun insertCategory(name: String) = databaseClient.execute("select insert_category('$name')")
        .fetch().one().map { it["insert_category"] as Int }

    fun insertBreed(name: String) = databaseClient.execute("select insert_breed('$name')")
        .fetch().one().map { it["insert_breed"] as Int }

    fun insertPet(cmd: Command, categoryId: Int, breedId: Int) = databaseClient.insert().into("pets")
        .value("id", cmd.id.toString())
        .value("name", cmd.get("name"))
        .value("dob", cmd.get("dob"))
        .value("category", categoryId)
        .value("breed", breedId)
        .then()
}
