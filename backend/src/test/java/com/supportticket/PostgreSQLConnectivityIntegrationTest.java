package com.supportticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;

import javax.sql.DataSource;

import com.supportticket.support.AbstractPostgreSQLContainerTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PostgreSQLConnectivityIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectsToPostgreSQL() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(5)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualToIgnoringCase("PostgreSQL");
        }
    }
}
