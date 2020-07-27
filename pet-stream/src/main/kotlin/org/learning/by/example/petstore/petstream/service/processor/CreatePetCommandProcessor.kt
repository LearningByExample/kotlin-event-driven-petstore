@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petstream.service.processor

import org.learning.by.example.petstore.command.Command
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

@Service
class CreatePetCommandProcessor(
    val databaseClient: DatabaseClient,
    val transactionalOperator: TransactionalOperator
) : CommandProcessor {
    override fun process(cmd: Command): Mono<Void> {
        val category = cmd.get<String>("category")
        val breed = cmd.get<String>("breed")
        val vaccines = cmd.getList<String>("vaccines")
        val tags = if (cmd.contains("tags")) cmd.getList<String>("tags") else listOf()

        return transactionalOperator.transactional(
            insertReferences(category, breed, vaccines, tags).then(
                insertPet(cmd).then(
                    insertVaccines(cmd.id.toString(), vaccines).then(
                        if (tags.isNotEmpty())
                            insertTags(cmd.id.toString(), tags)
                        else
                            Mono.empty<Void>()
                    )
                )
            ).onErrorMap {
                CreatePetException("error processing command : '$cmd'", it)
            }
        )
    }

    fun insertCategory(category: String) = insertIfNotExist("categories", category)

    fun insertBreed(breed: String) = insertIfNotExist("breeds", breed)

    fun insertTag(tag: String) = insertIfNotExist("tags", tag)

    fun insertVaccine(vaccine: String) = insertIfNotExist("vaccines", vaccine)

    fun insertTagsReference(tags: List<String>) = tags.toFlux().flatMap(::insertTag).collectList()

    fun insertVaccinesReference(vaccines: List<String>) = vaccines.toFlux().flatMap(::insertVaccine).collectList()

    fun insertPet(cmd: Command) = databaseClient.execute(
        """
        INSERT INTO pets (id, name, category, breed, dob)
        SELECT
            :id,
            :name,
            categories.id as category_id,
            breeds.id     as breed_id,
           :dob
        FROM
            categories,
            breeds
        WHERE
            categories.name = :category_name AND
                breeds.name = :breed_name
        """
    )
        .bind("id", cmd.id.toString())
        .bind("name", cmd.get("name"))
        .bind("dob", Instant.parse(cmd.get("dob")))
        .bind("category_name", cmd.get("category"))
        .bind("breed_name", cmd.get("breed"))
        .fetch()
        .rowsUpdated()

    fun insertVaccines(id: String, vaccines: List<String>) = databaseClient.execute(
        """
        INSERT
        INTO pets_vaccines(id_pet, id_vaccine)
        SELECT :pet_id, id
        FROM vaccines
        WHERE name IN (:vaccines)
        """
    )
        .bind("pet_id", id)
        .bind("vaccines", vaccines)
        .then()

    fun insertTags(id: String, tags: List<String>) = databaseClient.execute(
        """
        INSERT
        INTO pets_tags(id_pet, id_tag)
        SELECT :pet_id, id
        FROM tags
        WHERE name IN (:tags)
        """
    )
        .bind("pet_id", id)
        .bind("tags", tags)
        .then()

    fun insertReferences(category: String, breed: String, vaccines: List<String>, tags: List<String>) =
        insertCategory(category).then(
            insertBreed(breed).then(
                insertVaccinesReference(vaccines).then(
                    insertTagsReference(tags)
                )
            )
        )

    fun insertIfNotExist(table: String, name: String) = databaseClient.execute(
        """
        INSERT
        INTO $table (name)
        SELECT :name_value
        WHERE :name_value NOT IN
        (
            SELECT name
            FROM $table
            WHERE name = :name_value
        )
        """
    )
        .bind("name_value", name)
        .fetch()
        .rowsUpdated()

    override fun getCommandName() = "pet_create"

    override fun validate(cmd: Command) = cmd.contains("name") && cmd.contains("dob") && cmd.contains("category") &&
        cmd.contains("breed") && cmd.contains("tags") && cmd.contains("vaccines")
}
