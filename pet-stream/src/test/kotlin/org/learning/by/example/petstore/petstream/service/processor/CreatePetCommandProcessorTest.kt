@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petstream.service.processor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
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
    fun `should process create command with empty tags and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf()
        }

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
        verifyPetHasTags(cmd.id, cmd.getList("tags"))
        verifyPetHasVaccines(cmd.id, cmd.getList("vaccines"))
    }

    @Test
    fun `should process create command with not tags and save a pet in the database`() {
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
        verifyPetHasVaccines(cmd.id, cmd.getList("vaccines"))
    }

    @Test
    fun `should process create command with empty vaccines and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf()
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

    fun verifyPetIsSaved(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets")
                .project("id", "name", "dob", "category", "breed")
                .matching(where("id").isEquals(cmd.id.toString()))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["id"]).isEqualTo(cmd.id.toString())
            assertThat(it["name"]).isEqualTo(cmd.get("name"))
            assertThat(it["dob"]).isEqualTo(
                LocalDateTime.ofInstant(
                    Instant.parse(cmd.get("dob")),
                    ZoneOffset.systemDefault()
                )
            )
            assertThat(it["category"] as Int).isNotZero()
            verifyCategoryIsCorrect(it["category"] as Int, cmd.get("category"))
            verifyBreedIsCorrect(it["breed"] as Int, cmd.get("breed"))
        }.verifyComplete()
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

    fun verifyNameMatchValueInTableById(value: String, table: String, id: Int) {
        StepVerifier.create(
            databaseClient.select().from(table)
                .project("name")
                .matching(where("id").isEquals(id))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["name"]).isEqualTo(value)
        }.verifyComplete()
    }

    fun verifyCategoryIsCorrect(id: Int, category: String) = verifyNameMatchValueInTableById(category, "categories", id)
    fun verifyBreedIsCorrect(id: Int, breed: String) = verifyNameMatchValueInTableById(breed, "breeds", id)
    fun verifyTagIsCorrect(id: Int, tag: String) = verifyNameMatchValueInTableById(tag, "tags", id)
    fun verifyVaccineIsCorrect(id: Int, vaccine: String) = verifyNameMatchValueInTableById(vaccine, "vaccines", id)

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
            assertThat(it["name"]).isEqualTo(tag)
        }.verifyComplete()
    }

    fun verifyPetHasTags(id: UUID, tags: List<String>) {
        tags.forEach {
            verifyPetHasTag(id, it)
        }
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
            assertThat(it["name"]).isEqualTo(vaccine)
        }.verifyComplete()
    }

    fun verifyPetHasVaccines(id: UUID, vaccines: List<String>) {
        vaccines.forEach {
            verifyPetHasVaccine(id, it)
        }
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

    data class InsertIfExistCase(val table: String, val error: Boolean)

    @TestFactory
    fun `we should insert if not exist and keep already inserted`() = listOf(
        InsertIfExistCase(
            table = "categories",
            error = false
        ),
        InsertIfExistCase(
            table = "breeds",
            error = false
        ),
        InsertIfExistCase(
            table = "vaccines",
            error = false
        ),
        InsertIfExistCase(
            table = "tags",
            error = false
        ),
        InsertIfExistCase(
            table = "bad_table",
            error = true
        )
    ).map { case ->
        DynamicTest.dynamicTest("table ${case.table}") {
            if (!case.error) {
                var firstId = -1
                val firstValue = "${case.table}1"
                val secondValue = "${case.table}2"

                StepVerifier.create(createPetCommandProcessor.insertIfNotExist(case.table, firstValue))
                    .expectSubscription()
                    .consumeNextWith {
                        assertThat(it).isNotZero()
                        firstId = it
                        verifyNameMatchValueInTableById(firstValue, case.table, it)
                    }
                    .verifyComplete()

                StepVerifier.create(createPetCommandProcessor.insertIfNotExist(case.table, secondValue))
                    .expectSubscription()
                    .consumeNextWith {
                        assertThat(it).isNotZero()
                        assertThat(it).isNotEqualTo(firstId)
                        verifyNameMatchValueInTableById(secondValue, case.table, it)
                    }
                    .verifyComplete()

                StepVerifier.create(createPetCommandProcessor.insertIfNotExist(case.table, firstValue))
                    .expectSubscription()
                    .consumeNextWith {
                        assertThat(it).isNotZero()
                        assertThat(it).isEqualTo(firstId)
                        verifyNameMatchValueInTableById(firstValue, case.table, it)
                    }
                    .verifyComplete()
            } else {
                StepVerifier.create(createPetCommandProcessor.insertIfNotExist(case.table, "value"))
                    .expectSubscription()
                    .expectError<CreatePetException>()
                    .verify()
            }
        }
    }

    data class InsertIndividualFunCases(
        val name: String,
        val insertFunc: (String) -> Mono<Int>,
        val verifyFunc: (Int, String) -> Unit
    )

    @TestFactory
    fun `we should individual test each insert function`() = listOf(
        InsertIndividualFunCases(
            name = "breed",
            insertFunc = createPetCommandProcessor::insertBreed,
            verifyFunc = ::verifyBreedIsCorrect
        ),
        InsertIndividualFunCases(
            name = "category",
            insertFunc = createPetCommandProcessor::insertCategory,
            verifyFunc = ::verifyCategoryIsCorrect
        ),
        InsertIndividualFunCases(
            name = "vaccine",
            insertFunc = createPetCommandProcessor::insertVaccine,
            verifyFunc = ::verifyVaccineIsCorrect
        ),
        InsertIndividualFunCases(
            name = "tag",
            insertFunc = createPetCommandProcessor::insertTag,
            verifyFunc = ::verifyTagIsCorrect
        )
    ).map { case ->
        DynamicTest.dynamicTest(case.name) {
            StepVerifier
                .create(case.insertFunc(case.name))
                .expectSubscription()
                .consumeNextWith {
                    case.verifyFunc(it, case.name)
                }
                .verifyComplete()
        }
    }

    @Test
    fun `we should insert pets`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1")
        }

        val categoryId = createPetCommandProcessor.insertCategory(cmd.get("category")).block()!!
        val breedId = createPetCommandProcessor.insertBreed(cmd.get("breed")).block()!!

        StepVerifier.create(createPetCommandProcessor.insertPet(cmd, categoryId, breedId))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
    }

    @Test
    fun `we should add a tag to a pet`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1")
        }

        val categoryId = createPetCommandProcessor.insertCategory(cmd.get("category")).block()!!
        val breedId = createPetCommandProcessor.insertBreed(cmd.get("breed")).block()!!
        createPetCommandProcessor.insertPet(cmd, categoryId, breedId).block()

        StepVerifier.create(createPetCommandProcessor.addTagToPet(cmd.id, "tag1"))
            .expectSubscription()
            .verifyComplete()

        verifyPetHasTag(cmd.id, "tag1")
    }

    @Test
    fun `we should add tags to a pet`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        val categoryId = createPetCommandProcessor.insertCategory(cmd.get("category")).block()!!
        val breedId = createPetCommandProcessor.insertBreed(cmd.get("breed")).block()!!
        createPetCommandProcessor.insertPet(cmd, categoryId, breedId).block()

        val tags = cmd.getList<String>("tags")
        StepVerifier.create(createPetCommandProcessor.addTagsToPet(cmd.id, tags))
            .expectSubscription()
            .verifyComplete()
        verifyPetHasTags(cmd.id, tags)
    }

    @Test
    fun `we should add a vaccine to a pet`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1")
        }

        val categoryId = createPetCommandProcessor.insertCategory(cmd.get("category")).block()!!
        val breedId = createPetCommandProcessor.insertBreed(cmd.get("breed")).block()!!
        createPetCommandProcessor.insertPet(cmd, categoryId, breedId).block()

        StepVerifier.create(createPetCommandProcessor.addVaccineToPet(cmd.id, "vaccine1"))
            .expectSubscription()
            .verifyComplete()

        verifyPetHasVaccine(cmd.id, "vaccine1")
    }

    @Test
    fun `we should add vaccines to a pet`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        val categoryId = createPetCommandProcessor.insertCategory(cmd.get("category")).block()!!
        val breedId = createPetCommandProcessor.insertBreed(cmd.get("breed")).block()!!
        createPetCommandProcessor.insertPet(cmd, categoryId, breedId).block()

        val vaccines = cmd.getList<String>("vaccines")
        StepVerifier.create(createPetCommandProcessor.addVaccinesToPet(cmd.id, vaccines))
            .expectSubscription()
            .verifyComplete()
        verifyPetHasVaccines(cmd.id, vaccines)
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
            assertThat(createPetCommandProcessor.validate(it.cmd)).isEqualTo(it.expect)
        }
    }

    @Test
    fun `if we fail to insert category we will not insert the pet`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).insertCategory(any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to insert breed we will not insert the pet`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).insertBreed(any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to insert the pet we will not have other details`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).insertPet(any(), any(), any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to set the tags we will not have other details`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addTagsToPet(any(), any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to set the a tag we will not have other details`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addTagToPet(any(), any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to set a particular tag we will not have other details`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addTagToPet(cmd.id, "tag2")

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    @Test
    fun `if we fail to set the vaccines we will not have other details`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addVaccinesToPet(any(), any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to set a vaccine we will not have other details`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addVaccineToPet(any(), any())

        val cmd = command("pet_create") {
            "name" value "name"
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

    @Test
    fun `if we fail to set the vaccines without tags we will not have other details`() {
        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addVaccineToPet(any(), any())

        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
        }

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }

    @Test
    fun `if we fail to set a particular vaccine we will not have other details`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now().toString()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        doReturn(Mono.error<Int>(CreatePetException("Something Wrong happen")))
            .whenever(createPetCommandProcessor).addVaccineToPet(cmd.id, "vaccine2")

        StepVerifier.create(createPetCommandProcessor.process(cmd))
            .expectError<CreatePetException>()
            .verify()

        verifyPetIsNotSaved(cmd)
        verifyPetHasNotTags(cmd)
        verifyPetHasNotVaccines(cmd)
    }
}
