package org.learning.by.example.petstore.petcommands.routes

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.toMono

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class PetRoutesTest {
    @Autowired
    private lateinit var webClient: WebTestClient

    companion object {
        const val PET_URL = "/pet"
        private const val VALID_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
        const val VALID_PET_URL = "$PET_URL/$VALID_UUID"
        const val EXAMPLE_PET = """
            {
              "name": "dogie",
              "category": "string",
              "tags": [
                "string1",
                "string2",
              ],
            }
        """
    }

    data class TestCase(val name: String, val parameters: Parameters, val expect: Expect) {
        data class Parameters(val accept: MediaType, val contentType: MediaType)
        data class Expect(val status: HttpStatus)
    }

    @TestFactory
    fun `We should handle correctly accept and content type`() = listOf(
        TestCase(
            name = "we should be ok with correct types",
            parameters = TestCase.Parameters(
                accept = MediaType.APPLICATION_JSON_UTF8,
                contentType = MediaType.APPLICATION_JSON_UTF8
            ),
            expect = TestCase.Expect(status = HttpStatus.CREATED)
        ),
        TestCase(
            name = "we should not found with invalid accept",
            parameters = TestCase.Parameters(
                accept = MediaType.APPLICATION_JSON_UTF8,
                contentType = MediaType.TEXT_PLAIN
            ),
            expect = TestCase.Expect(status = HttpStatus.NOT_FOUND)
        ),
        TestCase(
            name = "we should not found with invalid content",
            parameters = TestCase.Parameters(
                accept = MediaType.TEXT_PLAIN,
                contentType = MediaType.APPLICATION_JSON_UTF8
            ),
            expect = TestCase.Expect(status = HttpStatus.NOT_FOUND)
        ),
        TestCase(
            name = "we should not found with invalid accept and content",
            parameters = TestCase.Parameters(
                accept = MediaType.TEXT_PLAIN,
                contentType = MediaType.TEXT_PLAIN
            ),
            expect = TestCase.Expect(status = HttpStatus.NOT_FOUND)
        )
    ).map {
        dynamicTest(it.name) {
            webClient.post()
                .uri(PET_URL)
                .accept(it.parameters.accept)
                .contentType(it.parameters.contentType)
                .body(EXAMPLE_PET.toMono(), EXAMPLE_PET.javaClass)
                .exchange()
                .expectStatus().isEqualTo(it.expect.status)
        }
    }

    @Test
    fun `we should get an id when adding a pet`() {
        webClient.post()
            .uri(PET_URL)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(EXAMPLE_PET.toMono(), EXAMPLE_PET.javaClass)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath("$.id").value<String> {
                assertThat(it).matches(VALID_UUID)
            }
    }

    @Test
    fun `we should get a location header when adding a pet`() {
        webClient.post()
            .uri(PET_URL)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(EXAMPLE_PET.toMono(), EXAMPLE_PET.javaClass)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, VALID_PET_URL)
            .expectBody()
    }
}


