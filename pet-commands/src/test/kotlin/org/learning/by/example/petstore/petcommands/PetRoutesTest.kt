package org.learning.by.example.petstore.petcommands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.toMono
import java.nio.charset.StandardCharsets

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class PetRoutesTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    inner class TestCase(val name: String,
                         val acceptType: MediaType,
                         val contentType: MediaType,
                         val statusExpected: HttpStatus,
                         val responseExpected: String?)

    @TestFactory
    fun `Run multiple tests`(): Collection<DynamicTest> {
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

        val list = listOf(
            TestCase(
                "we should get 200 when posting a pet",
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON,
                HttpStatus.OK,
                "hello"
            ),
            TestCase(
                "we should get 404 when posting not json",
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                HttpStatus.NOT_FOUND,
                null
            ),
            TestCase(
                "we should get 404 when accepting not json",
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_JSON,
                HttpStatus.NOT_FOUND,
                null
            )
        )

        return list.map {
            dynamicTest(it.name) {
                webClient.post()
                    .uri("/pet")
                    .accept(it.acceptType)
                    .contentType(it.contentType)
                    .body(body.toMono(), String::class.java)
                    .exchange()
                    .expectStatus().isEqualTo(it.statusExpected)
                    .expectBody()
                    .consumeWith { response ->
                        if (response.status == HttpStatus.OK) {
                            assertThat(response.responseBody).isNotNull()
                            assertThat(response.responseBody?.toString(StandardCharsets.UTF_8)).isEqualTo(body)
                        }
                    }
            }
        }.toList()
    }

}


