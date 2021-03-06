package org.learning.by.example.petstore.petcommands.routes

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.learning.by.example.petstore.petcommands.handlers.PetHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.kotlin.core.publisher.toMono

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class PetRoutesTest(@Autowired private val webClient: WebTestClient) {
    @SpyBean
    private lateinit var petHandlerSpy: PetHandler

    companion object {
        const val PET_URL = "/pet"
        const val NOT_FOUND_URL = "/not-found"
        const val EXAMPLE_RESOURCE =
            """
            {
              "name": "resource",
            }
        """
        const val EXAMPLE_RESPONSE =
            """
            {
                "ok" : true
            }
        """
    }

    @BeforeEach
    fun setup() {
        doReturn(
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(EXAMPLE_RESPONSE.toMono())
        ).whenever(petHandlerSpy).postPet(any())
    }

    @AfterEach
    fun tearDown() {
        reset(petHandlerSpy)
    }

    data class TestCase(val name: String, val parameters: Parameters, val expect: Expect) {
        data class Parameters(val accept: MediaType, val contentType: MediaType)
        data class Expect(val status: HttpStatus)
    }

    @TestFactory
    fun `We should handle correctly accept and content type when posting a pet`() = listOf(
        TestCase(
            name = "we should be ok with correct types",
            parameters = TestCase.Parameters(
                accept = MediaType.APPLICATION_JSON,
                contentType = MediaType.APPLICATION_JSON
            ),
            expect = TestCase.Expect(status = HttpStatus.OK)
        ),
        TestCase(
            name = "we should not found with invalid accept",
            parameters = TestCase.Parameters(
                accept = MediaType.APPLICATION_JSON,
                contentType = MediaType.TEXT_PLAIN
            ),
            expect = TestCase.Expect(status = HttpStatus.NOT_FOUND)
        ),
        TestCase(
            name = "we should not found with invalid content",
            parameters = TestCase.Parameters(
                accept = MediaType.TEXT_PLAIN,
                contentType = MediaType.APPLICATION_JSON
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
                .body(EXAMPLE_RESOURCE.toMono(), EXAMPLE_RESOURCE.javaClass)
                .exchange()
                .expectStatus().isEqualTo(it.expect.status)
        }
    }

    @Test
    fun `we should invoke postPet handler when posting a pet`() {
        webClient.post()
            .uri(PET_URL)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(EXAMPLE_RESPONSE.toMono(), EXAMPLE_RESPONSE.javaClass)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("\$.ok").isEqualTo(true)
        verify(petHandlerSpy).postPet(any())
        verifyNoMoreInteractions(petHandlerSpy)
    }

    @Test
    fun `we should not use the petHandler when an invalid url`() {
        webClient.post()
            .uri(NOT_FOUND_URL)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(EXAMPLE_RESPONSE.toMono(), EXAMPLE_RESPONSE.javaClass)
            .exchange()
            .expectStatus().isNotFound
        verifyNoMoreInteractions(petHandlerSpy)
    }
}
