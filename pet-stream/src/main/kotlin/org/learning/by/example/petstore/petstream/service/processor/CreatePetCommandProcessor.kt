@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petstream.service.processor

import org.learning.by.example.petstore.command.Command
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant
import java.util.UUID

@Service
class CreatePetCommandProcessor(val databaseClient: DatabaseClient) : CommandProcessor {
    override fun process(cmd: Command) = insertCategory(cmd.get("category")).flatMap { category ->
        insertBreed(cmd.get("breed")).flatMap { breed ->
            insertPet(cmd, category, breed).switchIfEmpty {
                if (cmd.contains("tags"))
                    addTagsToPet(cmd.id, cmd.getList("tags"))
                else
                    Mono.empty()
            }.switchIfEmpty {
                addVaccinesToPet(cmd.id, cmd.getList("vaccines"))
            }
        }
    }.onErrorResume { err: Throwable ->
        deleteVaccines(cmd).switchIfEmpty {
            deleteTags(cmd).switchIfEmpty {
                deletePet(cmd).switchIfEmpty {
                    Mono.error(err)
                }
            }
        }
    }

    override fun getCommandName() = "pet_create"

    override fun validate(cmd: Command) = cmd.contains("name") && cmd.contains("dob") && cmd.contains("category") &&
        cmd.contains("breed") && cmd.contains("tags") && cmd.contains("vaccines")

    fun insertCategory(name: String) = databaseClient.execute("select insert_category('$name')")
        .fetch().one().map { it["insert_category"] as Int }.onErrorMap {
            CreatePetException("error inserting category", it)
        }

    fun insertBreed(name: String) = databaseClient.execute("select insert_breed('$name')")
        .fetch().one().map { it["insert_breed"] as Int }.onErrorMap {
            CreatePetException("error inserting breed", it)
        }

    fun insertPet(cmd: Command, categoryId: Int, breedId: Int) = databaseClient.insert().into("pets")
        .value("id", cmd.id.toString())
        .value("name", cmd.get("name"))
        .value("dob", Instant.parse(cmd.get("dob")))
        .value("category", categoryId)
        .value("breed", breedId)
        .then().onErrorMap {
            CreatePetException("error inserting pet", it)
        }

    fun deletePet(cmd: Command) = databaseClient.delete().from("pets")
        .matching(where("id").isEquals(cmd.id.toString())).then().onErrorMap {
            CreatePetException("error deleting pet", it)
        }

    fun insertTag(name: String) = databaseClient.execute("select insert_tag('$name')")
        .fetch().one().map { it["insert_tag"] as Int }.onErrorMap {
            CreatePetException("error inserting tag", it)
        }

    fun addTagToPet(petId: UUID, tag: String) = insertTag(tag).flatMap {
        databaseClient.insert().into("pets_tags")
            .value("id_pet", petId.toString())
            .value("id_tag", it)
            .then().onErrorMap { err ->
                CreatePetException("error adding tag to pet", err)
            }
    }

    fun addTagsToPet(petId: UUID, tags: List<String>) = tags.toFlux().flatMap {
        addTagToPet(petId, it)
    }.collectList().flatMap {
        Mono.empty<Void>()
    }.onErrorMap {
        CreatePetException("error adding tags to pet", it)
    }

    fun deleteTags(cmd: Command) = databaseClient.delete().from("pets_tags")
        .matching(where("id_pet").isEquals(cmd.id.toString())).then().onErrorMap {
            CreatePetException("error deleting tags", it)
        }

    fun insertVaccine(name: String) = databaseClient.execute("select insert_vaccine('$name')")
        .fetch().one().map { it["insert_vaccine"] as Int }

    fun addVaccineToPet(petId: UUID, vaccine: String) = insertVaccine(vaccine).flatMap {
        databaseClient.insert().into("pets_vaccines")
            .value("id_pet", petId.toString())
            .value("id_vaccine", it)
            .then().onErrorMap { err ->
                CreatePetException("error adding vaccine to pet", err)
            }
    }

    fun addVaccinesToPet(petId: UUID, vaccines: List<String>) = vaccines.toFlux().flatMap {
        addVaccineToPet(petId, it)
    }.collectList().flatMap {
        Mono.empty<Void>()
    }.onErrorMap {
        CreatePetException("error adding vaccines to pet", it)
    }

    fun deleteVaccines(cmd: Command) = databaseClient.delete().from("pets_vaccines")
        .matching(where("id_pet").isEquals(cmd.id.toString())).then().onErrorMap {
            CreatePetException("error deleting vaccines", it)
        }
}
