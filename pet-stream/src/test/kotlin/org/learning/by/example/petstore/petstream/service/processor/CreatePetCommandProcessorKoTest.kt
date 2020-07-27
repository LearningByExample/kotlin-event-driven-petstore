@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petstream.service.processor

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.test.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.core.isIn
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

    fun verifyReferenceIsNoSaved(table: String, values: List<String>) {
        StepVerifier.create(
            databaseClient
                .select().from(table)
                .project("name")
                .matching(where("name").isIn(values))
                .fetch().all()
        ).expectNextCount(0).verifyComplete()
    }

    fun verifyVaccinesReferencesAreNotSaved(cmd: Command) =
        verifyReferenceIsNoSaved("vaccines", cmd.getList("vaccines"))

    fun verifyTagsReferencesAreNotSaved(cmd: Command) =
        if (cmd.contains("tags")) verifyReferenceIsNoSaved("tags", cmd.getList("tags")) else Unit

    fun verifyCategoryReferencesIsNotSaved(cmd: Command) =
        verifyReferenceIsNoSaved("categories", listOf(cmd.get("category")))

    fun verifyBreedReferenceIsNotSaved(cmd: Command) =
        verifyReferenceIsNoSaved("breeds", listOf(cmd.get("breed")))

    data class TestCase(val name: String, val cmd: Command)

    @TestFactory
    fun `we should get errors from the database and have not details saved`() = listOf(
        TestCase(
            name = "insert category fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value ""
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            }
        ),
        TestCase(
            name = "insert category with no tags fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value ""
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
            }
        ),
        TestCase(
            name = "insert bread fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value ""
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            }
        ),
        TestCase(
            name = "insert bread with no tags fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value ""
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
            }
        ),
        TestCase(
            name = "insert tags fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "", "tag3")
            }
        ),
        TestCase(
            name = "insert vaccines fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            }
        ),
        TestCase(
            name = "insert vaccines with no tags fails",
            cmd = command("pet_create") {
                "name" value "name"
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("", "vaccine2")
                "dob" value Instant.now().toString()
            }
        ),
        TestCase(
            name = "insert pet fails",
            cmd = command("pet_create") {
                "name" value ""
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
                "tags" values listOf("tag1", "tag2", "tag3")
            }
        ),
        TestCase(
            name = "insert pet with no tag fails",
            cmd = command("pet_create") {
                "name" value ""
                "category" value "category"
                "breed" value "breed"
                "vaccines" values listOf("vaccine1", "vaccine2")
                "dob" value Instant.now().toString()
            }
        )
    ).map {
        DynamicTest.dynamicTest(it.name) {
            StepVerifier.create(createPetCommandProcessor.process(it.cmd))
                .expectError<CreatePetException>()
                .verify()
            with(it.cmd) {
                verifyPetIsNotSaved(this)
                verifyPetHasNotTags(this)
                verifyPetHasNotVaccines(this)
                verifyTagsReferencesAreNotSaved(this)
                verifyVaccinesReferencesAreNotSaved(this)
                verifyCategoryReferencesIsNotSaved(this)
                verifyBreedReferenceIsNotSaved(this)
            }
        }
    }
}
