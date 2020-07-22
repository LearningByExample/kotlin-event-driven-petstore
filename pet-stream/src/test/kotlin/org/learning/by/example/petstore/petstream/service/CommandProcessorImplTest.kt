@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petstream.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.listener.StreamListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@Testcontainers
internal class CommandProcessorImplTest(
    @Autowired val commandProcessorImpl: CommandProcessorImpl,
    @Autowired val databaseClient: DatabaseClient
) {
    companion object {
        @Container
        val container: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>().apply {
            withDatabaseName("pets")
            withUsername("petuser")
            withPassword("petpwd")
        }

        @DynamicPropertySource
        @JvmStatic
        fun setupProperties(registry: DynamicPropertyRegistry) = with(container) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://$host:$firstMappedPort/$databaseName"
            }
            registry.add("spring.r2dbc.username", ::getUsername)
            registry.add("spring.r2dbc.password", ::getPassword)
            registry.add("db.initialize") { "true" }
        }
    }

    @MockBean
    lateinit var streamListener: StreamListener

    @BeforeEach
    fun setUp() {
        doNothing().whenever(streamListener).onApplicationEvent(any())
    }

    @Test
    fun `should process create command and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        StepVerifier.create(commandProcessorImpl.process(cmd))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
        verifyPetHasTags(cmd.id, cmd.getList("tags"))
        verifyPetHasVaccines(cmd.id, cmd.getList("vaccines"))
    }

    @Test
    fun `should process create command without tags and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf()
        }

        StepVerifier.create(commandProcessorImpl.process(cmd))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
        verifyPetHasTags(cmd.id, cmd.getList("tags"))
        verifyPetHasVaccines(cmd.id, cmd.getList("vaccines"))
    }

    @Test
    fun `should process create command without vaccines and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf()
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        StepVerifier.create(commandProcessorImpl.process(cmd))
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
            assertThat(it["dob"]).isEqualTo(cmd.get<LocalDateTime>("dob"))
            assertThat(it["category"] as Int).isNotZero()
            verifyCategoryIsCorrect(it["category"] as Int, cmd.get("category"))
            verifyBreedIsCorrect(it["breed"] as Int, cmd.get("breed"))
        }.verifyComplete()
    }

    fun verifyCategoryIsCorrect(id: Int, category: String) {
        StepVerifier.create(
            databaseClient.select().from("categories")
                .project("name")
                .matching(where("id").isEquals(id))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["name"]).isEqualTo(category)
        }.verifyComplete()
    }

    fun verifyBreedIsCorrect(id: Int, breed: String) {
        StepVerifier.create(
            databaseClient.select().from("breeds")
                .project("name")
                .matching(where("id").isEquals(id))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["name"]).isEqualTo(breed)
        }.verifyComplete()
    }

    fun verifyTagIsCorrect(id: Int, tag: String) {
        StepVerifier.create(
            databaseClient.select().from("tags")
                .project("name")
                .matching(where("id").isEquals(id))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["name"]).isEqualTo(tag)
        }.verifyComplete()
    }

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

    @Test
    fun `we should insert categories and keep already inserted`() {
        var firstCategory = -1

        StepVerifier.create(commandProcessorImpl.insertCategory("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                firstCategory = it
                verifyCategoryIsCorrect(it, "one")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertCategory("two"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isNotEqualTo(firstCategory)
                verifyCategoryIsCorrect(it, "two")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertCategory("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isEqualTo(firstCategory)
                verifyCategoryIsCorrect(it, "one")
            }
            .verifyComplete()
    }

    @Test
    fun `we should insert breeds and keep already inserted`() {
        var firstBreed = -1

        StepVerifier.create(commandProcessorImpl.insertBreed("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                firstBreed = it
                verifyBreedIsCorrect(it, "one")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertBreed("two"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isNotEqualTo(firstBreed)
                verifyBreedIsCorrect(it, "two")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertBreed("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isEqualTo(firstBreed)
                verifyBreedIsCorrect(it, "one")
            }
            .verifyComplete()
    }

    @Test
    fun `we should insert pets`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1")
        }

        val categoryId = commandProcessorImpl.insertCategory(cmd.get("category")).block()!!
        val breedId = commandProcessorImpl.insertBreed(cmd.get("breed")).block()!!

        StepVerifier.create(commandProcessorImpl.insertPet(cmd, categoryId, breedId))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
    }

    @Test
    fun `we should insert tags and keep already inserted`() {
        var firstTag = -1

        StepVerifier.create(commandProcessorImpl.insertTag("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                firstTag = it
                verifyTagIsCorrect(it, "one")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertTag("two"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isNotEqualTo(firstTag)
                verifyTagIsCorrect(it, "two")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertTag("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isEqualTo(firstTag)
                verifyTagIsCorrect(it, "one")
            }
            .verifyComplete()
    }

    @Test
    fun `we should add a tag to a pet`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1")
        }

        val categoryId = commandProcessorImpl.insertCategory(cmd.get("category")).block()!!
        val breedId = commandProcessorImpl.insertBreed(cmd.get("breed")).block()!!
        commandProcessorImpl.insertPet(cmd, categoryId, breedId).block()

        StepVerifier.create(commandProcessorImpl.addTagToPet(cmd.id, "tag1"))
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
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        val categoryId = commandProcessorImpl.insertCategory(cmd.get("category")).block()!!
        val breedId = commandProcessorImpl.insertBreed(cmd.get("breed")).block()!!
        commandProcessorImpl.insertPet(cmd, categoryId, breedId).block()

        val tags = cmd.getList<String>("tags")
        StepVerifier.create(commandProcessorImpl.addTagsToPet(cmd.id, tags))
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
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1")
        }

        val categoryId = commandProcessorImpl.insertCategory(cmd.get("category")).block()!!
        val breedId = commandProcessorImpl.insertBreed(cmd.get("breed")).block()!!
        commandProcessorImpl.insertPet(cmd, categoryId, breedId).block()

        StepVerifier.create(commandProcessorImpl.addVaccineToPet(cmd.id, "vaccine1"))
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
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1", "tag2", "tag3")
        }

        val categoryId = commandProcessorImpl.insertCategory(cmd.get("category")).block()!!
        val breedId = commandProcessorImpl.insertBreed(cmd.get("breed")).block()!!
        commandProcessorImpl.insertPet(cmd, categoryId, breedId).block()

        val vaccines = cmd.getList<String>("vaccines")
        StepVerifier.create(commandProcessorImpl.addVaccinesToPet(cmd.id, vaccines))
            .expectSubscription()
            .verifyComplete()
        verifyPetHasVaccines(cmd.id, vaccines)
    }
}
