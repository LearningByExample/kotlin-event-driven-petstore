@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petqueries.configuration

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.learning.by.example.petstore.petqueries.testing.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria.where
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier

@SpringBootTest
internal class DBInitializerTest(@Autowired val databaseClient: DatabaseClient) : DatabaseTest() {
    companion object {
        val TABLES_TO_CHECK = arrayOf("categories", "breeds", "pets", "vaccines", "tags", "pets_vaccines", "pets_tags")
    }

    fun checkIfTableExist(name: String): Mono<Boolean> = databaseClient.select()
        .from("information_schema.tables")
        .project("table_name")
        .matching(
            where("table_name").isEquals(name).and("table_schema").isEquals("public")
        )
        .fetch().one().map { it.getOrDefault("table_name", "") == name }.switchIfEmpty(false.toMono())

    fun verifyTableHasRows(table: String, rows: Long) {
        StepVerifier.create(databaseClient.select()
            .from(table)
            .project("id")
            .fetch().all())
            .expectNextCount(rows).verifyComplete()
    }

    @TestFactory
    fun `we should have the tables created`() = TABLES_TO_CHECK.map {
        DynamicTest.dynamicTest("we should have the table '$it' created") {
            StepVerifier
                .create(checkIfTableExist(it))
                .expectSubscription()
                .expectNext(true)
                .verifyComplete()
        }
    }

    @Test
    fun `we should not have a no existing table created`() {
        StepVerifier
            .create(checkIfTableExist("no_pets"))
            .expectSubscription()
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `we should have inserted the data`() {
        verifyTableHasRows("pets", 1)
        verifyTableHasRows("breeds", 1)
        verifyTableHasRows("categories", 1)
    }
}
