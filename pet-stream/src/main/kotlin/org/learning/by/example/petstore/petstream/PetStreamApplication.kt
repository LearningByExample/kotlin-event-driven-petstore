package org.learning.by.example.petstore.petstream

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@SpringBootApplication
@EnableTransactionManagement
class PetStreamApplication {
    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionalOperator(connectionFactory: ConnectionFactory) =
        TransactionalOperator.create(R2dbcTransactionManager(connectionFactory))
}

fun main(args: Array<String>) {
    runApplication<PetStreamApplication>(*args)
}
