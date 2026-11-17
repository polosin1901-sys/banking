package com.bankingApp.offersToClients.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComparisonOperatorTest {

    @ParameterizedTest
    @CsvSource({
            ">, 5, 3, true",
            ">, 3, 5, false",
            "<, 3, 5, true",
            "<, 5, 3, false",
            "=, 4, 4, true",
            "=, 4, 5, false",
            ">=, 4, 4, true",
            ">=, 3, 4, false",
            "<=, 4, 4, true",
            "<=, 5, 4, false"
    })
    void appliesOperatorToOperands(String symbol, int left, int right, boolean expected) {
        assertThat(ComparisonOperator.fromString(symbol).test(left, right))
                .isEqualTo(expected);
    }

    @Test
    void parsesEverySupportedSymbol() {
        assertThat(ComparisonOperator.fromString(">")).isEqualTo(ComparisonOperator.GT);
        assertThat(ComparisonOperator.fromString("<")).isEqualTo(ComparisonOperator.LT);
        assertThat(ComparisonOperator.fromString("=")).isEqualTo(ComparisonOperator.EQ);
        assertThat(ComparisonOperator.fromString(">=")).isEqualTo(ComparisonOperator.GE);
        assertThat(ComparisonOperator.fromString("<=")).isEqualTo(ComparisonOperator.LE);
    }

    @Test
    void rejectsUnsupportedSymbol() {
        assertThatThrownBy(() -> ComparisonOperator.fromString("!="))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("!=");
    }
}
