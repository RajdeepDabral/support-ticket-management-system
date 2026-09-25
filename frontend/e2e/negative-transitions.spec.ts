import { expect, test } from '@playwright/test';
import { attemptTransition, createTicket, getTicket, transitionTicketStatus } from './helpers/api-client';
import { createTestRunId, ticketTitle } from './helpers/test-data';

const INVALID_TRANSITION_CASES = [
  { from: 'OPEN', to: 'RESOLVED' },
  { from: 'OPEN', to: 'CLOSED' },
  { from: 'CLOSED', to: 'OPEN' },
  { from: 'RESOLVED', to: 'OPEN' },
  { from: 'CANCELLED', to: 'OPEN' },
] as const;

test.describe('Negative state-machine transitions', () => {
  for (const transitionCase of INVALID_TRANSITION_CASES) {
    test(`rejects ${transitionCase.from} -> ${transitionCase.to} via backend API`, async ({ request }) => {
      const runId = createTestRunId();
      const ticket = await createTicket(request, {
        title: ticketTitle(runId, `Invalid ${transitionCase.from} to ${transitionCase.to}`),
        description: 'Ticket for invalid transition verification.',
        priority: 'MEDIUM',
        assignee: 'negative.tester',
      });

      if (transitionCase.from !== 'OPEN') {
        const setupTransitions: Record<string, string[]> = {
          CLOSED: ['IN_PROGRESS', 'RESOLVED', 'CLOSED'],
          RESOLVED: ['IN_PROGRESS', 'RESOLVED'],
          CANCELLED: ['IN_PROGRESS', 'CANCELLED'],
        };
        const steps = setupTransitions[transitionCase.from] ?? [];
        for (const step of steps) {
          await transitionTicketStatus(request, ticket.id, step);
        }
      }

      const before = await getTicket(request, ticket.id);
      expect(before.status).toBe(transitionCase.from);

      const result = await attemptTransition(request, ticket.id, transitionCase.to);
      expect(result.status).toBe(409);
      expect(result.body.code).toBe('INVALID_STATUS_TRANSITION');

      const after = await getTicket(request, ticket.id);
      expect(after.status).toBe(transitionCase.from);
    });
  }
});
