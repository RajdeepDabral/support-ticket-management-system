import { expect, test } from '@playwright/test';
import { createTicket } from './helpers/api-client';
import { createTestRunId, ticketTitle } from './helpers/test-data';
import {
  acceptConfirmationDialogs,
  createTicketViaUi,
  expectTicketDetails,
  openApplication,
  transitionStatusViaUi,
} from './helpers/ui-actions';

test.describe('Cancellation workflow', () => {
  test('cancels an OPEN ticket', async ({ page }) => {
    acceptConfirmationDialogs(page);

    const runId = createTestRunId();
    const title = ticketTitle(runId, 'Cancel from open');

    await openApplication(page);
    await createTicketViaUi(page, {
      title,
      description: 'Ticket to cancel before work starts.',
      priority: 'MEDIUM',
      assignee: 'cancel.tester',
    });

    await transitionStatusViaUi(page, 'Cancel Ticket');
    await expect(page.getByText('Ticket status updated successfully.')).toBeVisible();
    await expectTicketDetails(page, {
      title,
      description: 'Ticket to cancel before work starts.',
      priority: 'MEDIUM',
      status: 'CANCELLED',
      assignee: 'cancel.tester',
    });
    await expect(page.getByText('No status actions available.')).toBeVisible();
  });

  test('cancels an IN_PROGRESS ticket', async ({ page, request }) => {
    acceptConfirmationDialogs(page);

    const runId = createTestRunId();
    const title = ticketTitle(runId, 'Cancel from in progress');

    const ticket = await createTicket(request, {
      title,
      description: 'Ticket cancelled after work started.',
      priority: 'HIGH',
      assignee: 'cancel.tester',
    });

    await openApplication(page);
    await page.goto(`/tickets/${ticket.id}`);

    await transitionStatusViaUi(page, 'Start Progress');
    await expectTicketDetails(page, {
      title,
      description: 'Ticket cancelled after work started.',
      priority: 'HIGH',
      status: 'IN_PROGRESS',
      assignee: 'cancel.tester',
    });

    await transitionStatusViaUi(page, 'Cancel Ticket');
    await expectTicketDetails(page, {
      title,
      description: 'Ticket cancelled after work started.',
      priority: 'HIGH',
      status: 'CANCELLED',
      assignee: 'cancel.tester',
    });
    await expect(page.getByText('No status actions available.')).toBeVisible();
  });
});
