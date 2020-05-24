package org.learning.by.example.petstore.petcommands

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.toMono

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class PetCommandsApplicationTests (@Autowired private val webClient: WebTestClient){

    @Test
    fun contextLoads() {
    }

    @Test
    fun `we should get 200 when posting a pet`() {
        val body = """
            {
              "name": "doggie",
              "category": "string",
              "tags": [
                "string1",
                "string2",
              ],
            }
        """.trimIndent()

        webClient.post()
            .uri("/pet")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body.toMono(), String::class.java)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `we should get 404 when posting not json`() {
        val body = """
            {
              "name": "doggie",
              "category": "string",
              "tags": [
                "string1",
                "string2",
              ],
            }
        """.trimIndent()

        webClient.post()
            .uri("/pet")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.TEXT_PLAIN)
            .body(body.toMono(), String::class.java)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `we should get 404 when accepting not json`() {
        val body = """
            {
              "name": "doggie",
              "category": "string",
              "tags": [
                "string1",
                "string2",
              ],
            }
        """.trimIndent()

        webClient.post()
            .uri("/pet")
            .accept(MediaType.TEXT_PLAIN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body.toMono(), String::class.java)
            .exchange()
            .expectStatus().isNotFound
    }


}
