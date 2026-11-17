package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.model.UserInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reads client profiles from the recommendations (H2) database.
 *
 * <p>Kept separate from {@link RecommendationsRepository} so that the latter exposes exactly the
 * three aggregate queries used by recommendation rules.</p>
 */
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

    /**
     * Finds a client by Telegram username.
     *
     * <p>Only an unambiguous match is returned: zero rows and more than one row both produce
     * {@link Optional#empty()}. The query fetches at most two rows, which is enough to detect
     * duplicates without reading the whole table.</p>
     *
     * @param username username without the leading {@code @}
     * @return the single matching client, or empty when the result is ambiguous
     */
    public Optional<UserInfo> findByUsername(String username) {
        if (username == null || username.isBlank()) {
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
                username.trim()
        );

        return users.size() == 1 ? Optional.of(users.get(0)) : Optional.empty();
    }
}
