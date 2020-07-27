@file:Suppress("DEPRECATION")
package org.learning.by.example.petstore.petstream.service.processor

import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.test.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria.where
import reactor.kotlin.test.expectError
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
internal class CreatePetCommandProcessorKoTest(
    @Autowired val databaseClient: DatabaseClient,
    @Autowired val createPetCommandProcessor: CreatePetCommandProcessor
) : DatabaseTest() {
    fun verifyPetHasNotTags(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets_tags")
                .project("id_pet")
                .matching(where("id_pet").isEquals(cmd.id.toString()))
                .fetch().all()
        ).expectNextCount(0).verifyComplete()
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

    @Test
    fun `should return an exception when insert category fails`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value ""
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    @Test
    fun `should return an exception when insert breed fails`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value ""
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

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
            "tags" values listOf("tag1", "", "tag3")
        }

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
            "vaccines" values listOf("", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

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
            "name" value ""
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }
}
