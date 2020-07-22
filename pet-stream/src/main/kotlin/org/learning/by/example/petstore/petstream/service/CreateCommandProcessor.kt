package org.learning.by.example.petstore.petstream.service

import org.learning.by.example.petstore.command.Command
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import java.util.UUID

@Service
class CreateCommandProcessor(val databaseClient: DatabaseClient) : CommandProcessor {
    override fun process(cmd: Command) = insertCategory(cmd.get("category")).flatMap { category ->
        insertBreed(cmd.get("breed")).flatMap { breed ->
            insertPet(cmd, category, breed)
        }.switchIfEmpty {
            if (cmd.contains("tags"))
                addTagsToPet(cmd.id, cmd.getList("tags"))
            else
                Mono.empty()
        }.switchIfEmpty {
            addVaccinesToPet(cmd.id, cmd.getList("vaccines"))
        }
    }

    override fun getCommandName() = "pet_create"

    override fun validate(cmd: Command) = cmd.contains("name") && cmd.contains("dob") && cmd.contains("category") &&
        cmd.contains("breed") && cmd.contains("tags") && cmd.contains("vaccines")

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

    fun insertTag(name: String) = databaseClient.execute("select insert_tag('$name')")
        .fetch().one().map { it["insert_tag"] as Int }

    fun addTagToPet(petId: UUID, tag: String) = insertTag(tag).flatMap {
        databaseClient.insert().into("pets_tags")
            .value("id_pet", petId.toString())
            .value("id_tag", it)
            .then()
    }

    fun addTagsToPet(petId: UUID, tags: List<String>) = tags.toFlux().flatMap {
        addTagToPet(petId, it)
    }.collectList().flatMap {
        Mono.empty<Void>()
    }

    fun insertVaccine(name: String) = databaseClient.execute("select insert_vaccine('$name')")
        .fetch().one().map { it["insert_vaccine"] as Int }

    fun addVaccineToPet(petId: UUID, vaccine: String) = insertVaccine(vaccine).flatMap {
        databaseClient.insert().into("pets_vaccines")
            .value("id_pet", petId.toString())
            .value("id_vaccine", it)
            .then()
    }

    fun addVaccinesToPet(petId: UUID, vaccines: List<String>) = vaccines.toFlux().flatMap {
        addVaccineToPet(petId, it)
    }.collectList().flatMap {
        Mono.empty<Void>()
    }
}
