package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.model.UserInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsersRepository {

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, first_name, last_name
            FROM users
            WHERE username = ?
            LIMIT 2
            """;

    private final JdbcTemplate jdbcTemplate;

    public UsersRepository(
            @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserInfo> findByUsername(String userName) {
        if (userName == null || userName.isBlank()) {
            return Optional.empty();
        }
        List<UserInfo> users = jdbcTemplate.query(
                FIND_BY_USERNAME_SQL,
                (rs, rowNum) -> new UserInfo(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ),
                userName.trim()
        );
        return users.size() == 1 ? Optional.of(users.get(0)) : Optional.empty();
    }
}
