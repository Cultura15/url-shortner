package com.jesson.redirect_service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReadOnlyDatabaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void redirectServiceDbRoleCannotInsert() {
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.execute(
                        "INSERT INTO url_mapping (id, short_code, long_url, created_at) " +
                                "VALUES (999999, 'hack', 'https://evil.com', now())"
                )
        );

        Throwable cause = exception.getMostSpecificCause();

        assertInstanceOf(PSQLException.class, cause);

        PSQLException postgresException = (PSQLException) cause;

        assertEquals("42501", postgresException.getSQLState());
    }
}
