package org.learning.by.example.petstore.petqueries.service

import org.learning.by.example.petstore.petqueries.model.Pet
import org.slf4j.LoggerFactory
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
        val LOGGER = LoggerFactory.getLogger(PetServiceImpl::class.java)!!
        const val ERROR_PET_VACCINES = "Error getting pet vaccines"
        const val ERROR_PET_TAGS = "Error getting pet tags"
        const val ERROR_PET = "Error getting pet"
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
        const val SQL_SELECT_VACCINES =
            """
            SELECT
                vaccines.name
            FROM
                pets_vaccines, vaccines
            WHERE
                pets_vaccines.id_pet = :id
            AND
                pets_vaccines.id_vaccine = vaccines.id
        """
        const val SQL_SELECT_TAGS =
            """
            SELECT
                tags.name
            FROM
                pets_tags, tags
            WHERE
                pets_tags.id_pet = :id
            AND
                pets_tags.id_tag = tags.id
        """
    }

    override fun findPetById(id: UUID) = databaseClient.execute(SQL_SELECT_PET)
        .bind("id", id.toString())
        .fetch().one()
        .flatMap {
            val name = it.getValue("name") as String
            val category = it.getValue("category") as String
            val breed = it.getValue("breed") as String
            val dob = it.getValue("dob") as LocalDateTime
            val dobUtcString = dob.toInstant(ZoneOffset.UTC).toString()
            getVaccines(id).collectList().flatMap { vaccines ->
                if (vaccines.isNotEmpty()) {
                    getTags(id).collectList().flatMap { tags ->
                        Pet(name, category, breed, dobUtcString, vaccines, if (tags.isNotEmpty()) tags else null)
                            .toMono()
                    }
                } else {
                    LOGGER.warn("The pet with id $id was found without vaccines.")
                    Mono.empty()
                }
            }
        }.onErrorMap {
            GettingPetException(ERROR_PET, it)
        }

    fun getVaccines(id: UUID) = databaseClient.execute(SQL_SELECT_VACCINES)
        .bind("id", id.toString())
        .fetch().all()
        .flatMap {
            (it.getValue("name") as String).toMono()
        }.onErrorMap {
            GettingVaccinesException(ERROR_PET_VACCINES, it)
        }

    fun getTags(id: UUID) = databaseClient.execute(SQL_SELECT_TAGS)
        .bind("id", id.toString())
        .fetch().all()
        .flatMap {
            (it.getValue("name") as String).toMono()
        }.onErrorMap {
            GettingTagsException(ERROR_PET_TAGS, it)
        }
}
