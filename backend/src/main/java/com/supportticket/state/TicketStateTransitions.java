package com.supportticket.state;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.supportticket.domain.TicketStatus;

/**
 * Authoritative allow-list of permitted ticket status transitions.
 */
public final class TicketStateTransitions {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = buildAllowedTransitions();

    private TicketStateTransitions() {
    }

    private static Map<TicketStatus, Set<TicketStatus>> buildAllowedTransitions() {
        Map<TicketStatus, Set<TicketStatus>> transitions = new EnumMap<>(TicketStatus.class);
        transitions.put(TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED));
        transitions.put(TicketStatus.CLOSED, Set.of());
        transitions.put(TicketStatus.CANCELLED, Set.of());
        return Map.copyOf(transitions);
    }

    public static boolean isAllowed(TicketStatus currentStatus, TicketStatus requestedStatus) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(currentStatus, Set.of())
                .contains(requestedStatus);
    }
}
