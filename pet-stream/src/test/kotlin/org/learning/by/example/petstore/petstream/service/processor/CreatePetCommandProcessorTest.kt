@file:Suppress("DEPRECATION")
package org.learning.by.example.petstore.petstream.service.processor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.test.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria.where
import reactor.core.publisher.Mono
import reactor.kotlin.test.expectError
import reactor.test.StepVerifier
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@SpringBootTest
internal class CreatePetCommandProcessorTest(@Autowired val databaseClient: DatabaseClient) : DatabaseTest() {
    @SpyBean
    lateinit var createPetCommandProcessor: CreatePetCommandProcessor

    @AfterEach
    fun tearDown() {
        reset(createPetCommandProcessor)
    }

    @Test
    fun `should process create command and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
        verifyPetHasTags(cmd.id, cmd.getList("tags"))
        verifyPetHasVaccines(cmd.id, cmd.getList("vaccines"))
    }

    @Test
    fun `should process create command and save a pet without tags in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
        }

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasVaccines(cmd.id, cmd.getList("vaccines"))
    }

    fun verifyPetHasNotTags(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets_tags")
                .project("id_pet")
                .matching(where("id_pet").isEquals(cmd.id.toString()))
                .fetch().all()
        ).expectNextCount(0).verifyComplete()
    }

    fun verifyPetIsSaved(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets")
                .project("id", "name", "dob", "category", "breed")
                .matching(where("id").isEquals(cmd.id.toString()))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            Assertions.assertThat(it["id"]).isEqualTo(cmd.id.toString())
            Assertions.assertThat(it["name"]).isEqualTo(cmd.get("name"))
            Assertions.assertThat(it["dob"]).isEqualTo(
                LocalDateTime.ofInstant(
                    Instant.parse(cmd.get("dob")),
                    ZoneOffset.systemDefault()
                )
            )
            Assertions.assertThat(it["category"] as Int).isNotZero()
            verifyCategoryIsCorrect(it["category"] as Int, cmd.get("category"))
            verifyBreedIsCorrect(it["breed"] as Int, cmd.get("breed"))
        }.verifyComplete()
    }

    fun verifyNameMatchValueInTableById(value: String, table: String, id: Int) {
        StepVerifier.create(
            databaseClient.select().from(table)
                .project("name")
                .matching(where("id").isEquals(id))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            Assertions.assertThat(it["name"]).isEqualTo(value)
        }.verifyComplete()
    }

    fun verifyCategoryIsCorrect(id: Int, category: String) = verifyNameMatchValueInTableById(category, "categories", id)
    fun verifyBreedIsCorrect(id: Int, breed: String) = verifyNameMatchValueInTableById(breed, "breeds", id)

    fun verifyPetHasTag(id: UUID, tag: String) {
        StepVerifier.create(
            databaseClient
                .execute(
                    """
                    SELECT
                        name
                    FROM
                        tags, pets_tags
                    WHERE
                        pets_tags.id_pet = :id
                    AND
                        pets_tags.id_tag = tags.id
                    AND
                        tags.name = :name
                """
                )
                .bind("id", id.toString())
                .bind("name", tag)
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            Assertions.assertThat(it["name"]).isEqualTo(tag)
        }.verifyComplete()
    }

    fun verifyPetHasTags(id: UUID, tags: List<String>) {
        tags.forEach {
            verifyPetHasTag(id, it)
        }
    }

    fun verifyPetHasVaccine(id: UUID, vaccine: String) {
        StepVerifier.create(
            databaseClient
                .execute(
                    """
                    SELECT
                        name
                    FROM
                        vaccines, pets_vaccines
                    WHERE
                        pets_vaccines.id_pet = :id
                    AND
                        pets_vaccines.id_vaccine = vaccines.id
                    AND
                        vaccines.name = :name
                """
                )
                .bind("id", id.toString())
                .bind("name", vaccine)
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            Assertions.assertThat(it["name"]).isEqualTo(vaccine)
        }.verifyComplete()
    }

    fun verifyPetHasVaccines(id: UUID, vaccines: List<String>) {
        vaccines.forEach {
            verifyPetHasVaccine(id, it)
        }
    }

    data class ValidationCase(val case: String, val cmd: Command, val expect: Boolean)

    @TestFactory
    fun `we should validate create pet commands`() = listOf(
        ValidationCase(
            case = "all details correct should return true",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            },
            expect = true
        ),
        ValidationCase(
            case = "missing name should return false",
            cmd = command("pet_create") {
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            },
            expect = false
        ),
        ValidationCase(
            case = "missing category should return false",
            cmd = command("pet_create") {
                "name" value "name"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            },
            expect = false
        ),
        ValidationCase(
            case = "missing breed should return false",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            },
            expect = false
        ),
        ValidationCase(
            case = "missing vaccines should return false",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            },
            expect = false
        ),
        ValidationCase(
            case = "missing dob should return false",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "tags" values listOf("tag1", "tag2", "tag3")
            },
            expect = false
        ),
        ValidationCase(
            case = "missing tags should return true",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
            },
            expect = false
        )
    ).map {
        DynamicTest.dynamicTest(it.case) {
            Assertions.assertThat(createPetCommandProcessor.validate(it.cmd)).isEqualTo(it.expect)
        }
    }

    @Test
    fun `should return an exception when insert reference fails`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        doReturn(Mono.error<Void>(RuntimeException("something went wrong"))).whenever(createPetCommandProcessor)
            .insertReferences(any(), any(), any(), any())

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    @Test
    fun `should return an exception when insert tags fails`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        doReturn(Mono.error<Void>(RuntimeException("something went wrong"))).whenever(createPetCommandProcessor)
            .insertTags(any(), any())

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    @Test
    fun `should return an exception when insert vaccines fails`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        doReturn(Mono.error<Void>(RuntimeException("something went wrong"))).whenever(createPetCommandProcessor)
            .insertVaccines(any(), any())

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    @Test
    fun `should return an exception when insert pet fails`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        doReturn(Mono.error<Void>(RuntimeException("something went wrong"))).whenever(createPetCommandProcessor)
            .insertPet(any())

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    fun verifyPetIsNotSaved(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets")
                .project("id")
                .matching(where("id").isEquals(cmd.id.toString()))
                .fetch().one()
        ).expectNextCount(0).verifyComplete()
    }

    fun verifyPetHasNotVaccines(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets_vaccines")
                .project("id_pet")
                .matching(where("id_pet").isEquals(cmd.id.toString()))
                .fetch().all()
        ).expectNextCount(0).verifyComplete()
    }
}
