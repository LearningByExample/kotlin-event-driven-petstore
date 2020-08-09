package org.learning.by.example.petstore.petqueries.service

import org.learning.by.example.petstore.petqueries.model.Pet
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class PetServiceImpl(val databaseClient: DatabaseClient) : PetService {
    companion object {
        const val SQL_SELECT_PET =
            """
            SELECT
                 pets.id,
                 pets.name,
                 categories.name as category,
                 breeds.name as breed,
                 pets.dob
            FROM
                 pets, categories, breeds
            WHERE
                 pets.id = :id
            AND
                 pets.id_category =  categories.id
            AND
                 pets.id_breed = breeds.id
        """
    }
    override fun findPetById(id: UUID): Mono<Pet> {
        return databaseClient.execute(SQL_SELECT_PET)
            .bind("id", id.toString())
            .fetch().one()
            .flatMap {
                val name = it.getValue("name") as String
                val category = it.getValue("category") as String
                val breed = it.getValue("breed") as String
                val dob = it.getValue("dob") as LocalDateTime
                val dobUtcString = dob.toInstant(ZoneOffset.UTC).toString()
                Pet(name, category, breed, dobUtcString).toMono()
            }
    }
}
