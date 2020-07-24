package org.learning.by.example.petstore.petstream

import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petstream.test.BasicTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
class PetStreamApplicationTests(@Autowired val webTestClient: WebTestClient) : BasicTest() {
    @Test
    fun `should return not found when any request`() {
        webTestClient.get()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}
