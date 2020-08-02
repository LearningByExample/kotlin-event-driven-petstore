package org.learning.by.example.petstore.petqueries.routes

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petqueries.handler.PetHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse

@SpringBootTest
@AutoConfigureWebTestClient
internal class PetRoutesTest(@Autowired val webTestClient: WebTestClient) {
    companion object {
        const val INVALID_PATH = "/invalid"
        const val PET_GET_PATH = "/pet/{id}"
        const val EXISTING_PET_ID = "4cb5294b-1034-4bc4-9b3d-542adb232a21"
    }

    @SpyBean
    private lateinit var petHandlerSpy: PetHandler

    @BeforeEach
    fun setup() {
        doReturn(
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON).build()
        ).whenever(petHandlerSpy).getPet(any())
    }

    @AfterEach
    fun tearDown() {
        reset(petHandlerSpy)
    }

    @Test
    fun `should return a pet with a valid url`() {
        webTestClient.get()
            .uri {
                it.path(PET_GET_PATH)
                    .build(EXISTING_PET_ID)
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)

        verify(petHandlerSpy).getPet(any())
        verifyNoMoreInteractions(petHandlerSpy)
    }

    @Test
    fun `should return a not found error with an invalid url`() {
        webTestClient.get()
            .uri(INVALID_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)

        verifyNoMoreInteractions(petHandlerSpy)
    }
}
