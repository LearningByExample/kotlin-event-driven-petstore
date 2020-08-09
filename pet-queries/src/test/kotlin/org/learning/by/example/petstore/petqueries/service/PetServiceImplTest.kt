package org.learning.by.example.petstore.petqueries.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petqueries.testing.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
internal class PetServiceImplTest(@Autowired val petService: PetService) : DatabaseTest() {
    companion object {
        const val EXISTING_UUID = "4cb5294b-1034-4bc4-9b3d-542adb232a21"
        const val NOT_EXISTING_UUID = "44b5294b-1034-4b00-9b3d-542adb232a21"
    }

    @Test
    fun `we should find a pet in the database`() {
        StepVerifier.create(petService.findPetById(UUID.fromString(EXISTING_UUID)))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.name).isEqualTo("fluffy")
                assertThat(it.category).isEqualTo("dog")
                assertThat(it.breed).isEqualTo("german shepherd")
                assertThat(it.dob).isEqualTo("2020-08-09T10:35:07.981845Z")
            }
            .verifyComplete()
    }

    @Test
    fun `we should not find a pet in the database`() {
        StepVerifier.create(petService.findPetById(UUID.fromString(NOT_EXISTING_UUID)))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}
