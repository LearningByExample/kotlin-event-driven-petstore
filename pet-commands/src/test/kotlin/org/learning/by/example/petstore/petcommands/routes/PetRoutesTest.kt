package org.learning.by.example.petstore.petcommands.routes

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.learning.by.example.petstore.petcommands.handlers.PetHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.toMono
import java.net.URI

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class PetRoutesTest(@Autowired val webClient: WebTestClient) {

    @MockBean
    private lateinit var petHandler: PetHandler

    companion object {
        const val PET_URL = "/pet"
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
        const val HEADER_LOCATION_VALUE = "/location"
        const val ANY_BODY_VALUE = "any value is allow"
    }

    @BeforeEach
    fun setup() {
        val body = ServerResponse.created(URI.create(HEADER_LOCATION_VALUE))
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(ANY_BODY_VALUE.toMono())
        whenever(petHandler.postPet(any())).thenReturn(body)
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
    fun `we should return the handler's response`() {
        webClient.post()
            .uri(PET_URL)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(EXAMPLE_PET.toMono(), EXAMPLE_PET.javaClass)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .consumeWith {
                assertThat(it.responseBody!!.toString(Charsets.UTF_8)).isEqualTo(ANY_BODY_VALUE)
            }
    }
}


