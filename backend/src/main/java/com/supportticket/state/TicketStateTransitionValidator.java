package com.supportticket.state;

import com.supportticket.domain.TicketStatus;
import com.supportticket.exception.InvalidStatusTransitionException;

import org.springframework.stereotype.Component;

@Component
public class TicketStateTransitionValidator {

    public void validate(TicketStatus currentStatus, TicketStatus requestedStatus) {
        if (currentStatus == requestedStatus || !TicketStateTransitions.isAllowed(currentStatus, requestedStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, requestedStatus);
        }
    }
}
