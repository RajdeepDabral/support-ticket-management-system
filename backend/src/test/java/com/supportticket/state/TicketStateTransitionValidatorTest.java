package com.supportticket.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import com.supportticket.domain.TicketStatus;
import com.supportticket.exception.InvalidStatusTransitionException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketStateTransitionValidatorTest {

    private final TicketStateTransitionValidator validator = new TicketStateTransitionValidator();

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @MethodSource("validTransitions")
    void allowsValidTransitions(TicketStatus from, TicketStatus to) {
        assertThatCode(() -> validator.validate(from, to)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} -> {1} is rejected")
    @MethodSource("invalidTransitions")
    void rejectsInvalidTransitions(TicketStatus from, TicketStatus to) {
        assertThatThrownBy(() -> validator.validate(from, to))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Ticket cannot transition from " + from + " to " + to + ".")
                .satisfies(ex -> {
                    InvalidStatusTransitionException transitionException = (InvalidStatusTransitionException) ex;
                    assertThat(transitionException.getCurrentStatus()).isEqualTo(from);
                    assertThat(transitionException.getRequestedStatus()).isEqualTo(to);
                });
    }

    private static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    private static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CANCELLED));
    }
}
