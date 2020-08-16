package org.learning.by.example.petstore.petqueries.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petqueries.testing.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
internal class PetServiceImplTest(@Autowired val petServiceImpl: PetServiceImpl) : DatabaseTest() {
    companion object {
        const val EXISTING_UUID_WITHOUT_TAGS = "4cb5294b-1034-4bc4-9b3d-542adb232a21"
        const val EXISTING_UUID_WITH_TAGS = "4cb5294b-1034-abcd-9b3d-542adb232a21"
        const val NOT_EXISTING_UUID = "44b5294b-1034-4b00-9b3d-542adb232a21"
        const val EXISTING_UUID_WITHOUT_VACCINES = "4cb529ab-1034-4bc4-9b3d-542bdb232b21"
    }

    @Test
    fun `we should find a pet without tags in the database`() {
        StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(EXISTING_UUID_WITHOUT_TAGS)))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.name).isEqualTo("fluffy")
                assertThat(it.category).isEqualTo("dog")
                assertThat(it.breed).isEqualTo("german shepherd")
                assertThat(it.dob).isEqualTo("2020-08-09T10:35:07.981845Z")
                assertThat(it.vaccines).hasSize(3)
                assertThat(it.vaccines).containsAll(listOf("vaccine1", "vaccine2", "vaccine3"))
                assertThat(it.tags).isNull()
            }
            .verifyComplete()
    }

    @Test
    fun `we should find a pet with tags in the database`() {
        StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(EXISTING_UUID_WITH_TAGS)))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.name).isEqualTo("snowball")
                assertThat(it.category).isEqualTo("dog")
                assertThat(it.breed).isEqualTo("german shepherd")
                assertThat(it.dob).isEqualTo("2020-08-09T10:35:07.981845Z")
                assertThat(it.vaccines).hasSize(2)
                assertThat(it.vaccines).containsAll(listOf("vaccine1", "vaccine2"))
                assertThat(it.tags).hasSize(2)
                assertThat(it.tags).containsAll(listOf("brown", "small"))
            }
            .verifyComplete()
    }

    @Test
    fun `we should find a pet without vaccines in the database`() {
        StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(EXISTING_UUID_WITHOUT_VACCINES)))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `we should not find a pet in the database`() {
        StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(NOT_EXISTING_UUID)))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `we should get the list of vaccines for a pet`() {
        StepVerifier.create(petServiceImpl.getVaccines(UUID.fromString(EXISTING_UUID_WITHOUT_TAGS)))
            .expectSubscription()
            .expectNext("vaccine1", "vaccine2", "vaccine3")
            .verifyComplete()
    }

    @Test
    fun `we should not find vaccines for a pet`() {
        StepVerifier.create(petServiceImpl.getVaccines(UUID.fromString(NOT_EXISTING_UUID)))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `we should get the list of tags for a pet that has tags`() {
        StepVerifier.create(petServiceImpl.getTags(UUID.fromString(EXISTING_UUID_WITH_TAGS)))
            .expectSubscription()
            .expectNext("brown", "small")
            .verifyComplete()
    }

    @Test
    fun `we should not find tags for an existing pet that has no tags`() {
        StepVerifier.create(petServiceImpl.getTags(UUID.fromString(EXISTING_UUID_WITHOUT_TAGS)))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `we should not find tags for a pet that does not exist`() {
        StepVerifier.create(petServiceImpl.getTags(UUID.fromString(NOT_EXISTING_UUID)))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}
