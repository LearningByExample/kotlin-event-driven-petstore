package org.learning.by.example.petstore.petstream.configuration

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.FileSystemResource
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "db", name = ["initialize"], havingValue = "true")
class DBInitializer(connectionFactory: ConnectionFactory) : ConnectionFactoryInitializer() {
    companion object {
        const val SQL_SCHEMA_PATH = "../pet-sql/schema.sql"
    }

    init {
        this.setConnectionFactory(connectionFactory)
        this.setDatabasePopulator(
            CompositeDatabasePopulator().apply {
                addPopulators(ResourceDatabasePopulator(FileSystemResource(SQL_SCHEMA_PATH)))
            }
        )
    }
}
