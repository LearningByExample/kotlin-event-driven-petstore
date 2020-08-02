package org.learning.by.example.petstore.petqueries.routes

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
internal class PetRoutesTest(@Autowired val webTestClient: WebTestClient) {
    companion object {
        const val PET_GET_PATH = "/pet/{id}"
        const val EXISTING_PET_ID = "4cb5294b-1034-4bc4-9b3d-542adb232a21"
    }

    @Test
    fun `should return a pet`() {
        webTestClient.get()
            .uri {
                it.path(PET_GET_PATH)
                    .build(EXISTING_PET_ID)
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody()
            .jsonPath("\$.name").isEqualTo("fluffy")
            .jsonPath("\$.category").isEqualTo("dog")
            .jsonPath("\$.breed").isEqualTo("german shepherd")
            .jsonPath("\$.dob").isEqualTo("2020-06-28T00:00:00.0Z")
    }
}
