package org.learning.by.example.petstore.petcommands.handlers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.learning.by.example.petstore.petcommands.handlers.PetHandler.Companion.INVALID_RESOURCE
import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.service.PetCommands
import org.learning.by.example.petstore.petcommands.service.SendPetCreateException
import org.learning.by.example.petstore.petcommands.testing.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest
class PetHandlerTest(@Autowired private val petHandler: PetHandler) {
    companion object {
        const val VALID_PET =
            """
            {
              "name": "fluffy",
              "category": "dog",
              "breed": "german shepherd",
              "dob": "2020-06-28T00:00:00.0Z",
              "vaccines": [
                "rabies",
                "parvovirus",
                "distemper"
              ],
              "tags" : [
                "soft",
                "beauty",
                "good-boy"
              ]
            }
        """
        val FAKE_ID: UUID = UUID.randomUUID()
        const val ERROR_CAUSE = "Invalid Resource"
    }

    @MockBean
    private lateinit var petCommands: PetCommands

    @AfterEach
    fun tearDown() {
        reset(petCommands)
    }

    data class TestCase(val name: String, val parameters: Parameters, val expect: Expect) {
        data class Parameters(val body: String)
        data class Expect(val error: String)
    }

    @TestFactory
    fun `We should get bad request when adding a pet with bad input`(): List<DynamicTest> {
        doReturn(FAKE_ID.toMono()).whenever(petCommands).sendPetCreate(any())
        return listOf(
            TestCase(
                name = "we should get a bad request when trying to add a pet with empty name",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "",
                                  "category": "dog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid name, size must be between 3 and 20."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a long name",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "supersupersuperfluffy",
                                  "category": "dog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid name, size must be between 3 and 20."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a bad name",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "super fluffy",
                                  "category": "dog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid name, should be alphanumeric."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with no name",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "category": "dog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid name, must not be null."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with empty category",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy1",
                                  "category": "",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid category, size must be between 3 and 15."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a long category",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy2",
                                  "category": "megamegamegamegadog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid category, size must be between 3 and 15."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a bad category",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category": "dog1",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid category, should be only alphabetic characters."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with no category",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid category, must not be null."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a invalid tags",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "tags" : [ "do" ],
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid tags, each should be between 3 and 15 alphabetic characters or hyphen."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a valid & invalid tags",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ],
                                  "tags" : [ "beauty", "do" ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid tags, each should be between 3 and 15 alphabetic characters or hyphen."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a empty tag",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german shepherd",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ],
                                  "tags" : [ "" ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid tags, each should be between 3 and 15 alphabetic characters or hyphen."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with empty breed",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid breed, size must be between 5 and 25."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with no breed",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid breed, must not be null."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a bad breed",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german?",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid breed, should be alphabetic characters or space."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with long breed",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german long long long long",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid breed, size must be between 5 and 25."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a bad dob",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "dob": "2020-02-30T00:00:00.0Z",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid dob, should be a valid date."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with no dob",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid dob, must not be null."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with an empty dob",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "dob": "",
                                  "vaccines": [
                                    "rabies",
                                    "parvovirus",
                                    "distemper"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid dob, should be a valid date."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with no vaccines",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "dob": "2020-06-28T00:00:00.0Z"
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid vaccines, must not be null."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a bad vaccine",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "bad*value"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid vaccines, each should be between 5 and 50 alphabetic characters, hyphen or space."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with a valid & invalid vaccine",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [
                                    "good value",
                                    "bad*value"
                                  ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid vaccines, each should be between 5 and 50 alphabetic characters, hyphen or space."
                )
            ),
            TestCase(
                name = "we should get a bad request when trying to add a pet with an empty vaccine",
                parameters = TestCase.Parameters(
                    body =
                        """
                                {
                                  "name": "fluffy",
                                  "category" : "dog",
                                  "breed": "german",
                                  "dob": "2020-06-28T00:00:00.0Z",
                                  "vaccines": [ "" ]
                                }
                           """
                ),
                expect = TestCase.Expect(
                    error = "Invalid vaccines, each should be between 5 and 50 alphabetic characters, hyphen or space."
                )
            )
        ).map {
            DynamicTest.dynamicTest(it.name) {
                val httpRequest = MockServerHttpRequest
                    .post("/pet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(it.parameters.body)
                val webExchange = MockServerWebExchange.from(httpRequest)
                val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

                petHandler.postPet(request).verify { response, result: ErrorResponse ->
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)
                    assertThat(response.headers().location).isNull()

                    assertThat(result.message).isEqualTo(INVALID_RESOURCE)
                    assertThat(result.description).isEqualTo(it.expect.error)
                }
            }
        }
    }

    @Test
    fun `we should get the result and headers when adding a pet`() {
        // we may need to change this if this issue isn't fix :
        // https://github.com/spring-projects/spring-framework/issues/25087
        doReturn(FAKE_ID.toMono()).whenever(petCommands).sendPetCreate(any())
        val httpRequest = MockServerHttpRequest
            .post("/pet")
            .contentType(MediaType.APPLICATION_JSON)
            .body(VALID_PET)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.postPet(request).verify { response, result: Result ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
            assertThat(response.headers().location.toString()).isEqualTo("/pet/$FAKE_ID")
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(result.id).isEqualTo(FAKE_ID)
            verify(petCommands).sendPetCreate(any())
            verifyNoMoreInteractions(petCommands)
        }
    }

    @Test
    fun `we should get an error when something goes wrong calling a service`() {
        doReturn(Mono.error<Any>(RuntimeException(ERROR_CAUSE))).whenever(petCommands).sendPetCreate(any())
        val httpRequest = MockServerHttpRequest
            .post("/pet")
            .contentType(MediaType.APPLICATION_JSON)
            .body(VALID_PET)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.postPet(request).verify { response, error: ErrorResponse ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(error.message).isEqualTo("Server Error")
            assertThat(error.description).isEqualTo("Invalid Resource")

            verify(petCommands).sendPetCreate(any())
            verifyNoMoreInteractions(petCommands)
        }
    }

    @Test
    fun `we should get an specific error when getting a send pet create exception`() {
        doReturn(Mono.error<Any>(SendPetCreateException(RuntimeException()))).whenever(petCommands).sendPetCreate(any())
        val httpRequest = MockServerHttpRequest
            .post("/pet")
            .contentType(MediaType.APPLICATION_JSON)
            .body(VALID_PET)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.postPet(request).verify { response, error: ErrorResponse ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(error.message).isEqualTo("Error creating pet")
            assertThat(error.description).isEqualTo("Error sending pet create command")

            verify(petCommands).sendPetCreate(any())
            verifyNoMoreInteractions(petCommands)
        }
    }
}
