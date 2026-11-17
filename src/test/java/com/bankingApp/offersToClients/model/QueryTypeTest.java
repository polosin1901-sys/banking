package com.bankingApp.offersToClients.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryTypeTest {

    @ParameterizedTest
    @EnumSource(QueryType.class)
    void parsesItsOwnName(QueryType queryType) {
        assertThat(QueryType.fromString(queryType.name())).isEqualTo(queryType);
    }

    @Test
    void rejectsUnknownQueryType() {
        assertThatThrownBy(() -> QueryType.fromString("UNKNOWN_QUERY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN_QUERY");
    }

    @Test
    void isCaseSensitive() {
        assertThatThrownBy(() -> QueryType.fromString("user_of"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
