import { expect, test } from '@playwright/test';
import { createTicket, transitionTicketStatus } from './helpers/api-client';
import { createTestRunId, ticketTitle } from './helpers/test-data';
import {
  expectTicketRowHidden,
  expectTicketRowVisible,
  filterTicketsByStatus,
  openApplication,
  searchTickets,
  waitForTicketList,
} from './helpers/ui-actions';

test.describe('Search and filter', () => {
  const runId = createTestRunId();

  test.beforeAll(async ({ request }) => {
    await createTicket(request, {
      title: ticketTitle(runId, 'Payment gateway failure'),
      description: 'Customer cannot complete checkout payment.',
      priority: 'HIGH',
      assignee: 'search.tester',
    });

    const inProgressTicket = await createTicket(request, {
      title: ticketTitle(runId, 'LOGIN timeout'),
      description: 'Users report LOGIN delays during peak hours.',
      priority: 'MEDIUM',
      assignee: 'search.tester',
    });
    await transitionTicketStatus(request, inProgressTicket.id, 'IN_PROGRESS');

    const resolvedTicket = await createTicket(request, {
      title: ticketTitle(runId, 'Closed billing issue'),
      description: 'Resolved billing configuration mismatch.',
      priority: 'LOW',
      assignee: 'search.tester',
    });
    await transitionTicketStatus(request, resolvedTicket.id, 'IN_PROGRESS');
    await transitionTicketStatus(request, resolvedTicket.id, 'RESOLVED');
  });

  test('searches by title keyword', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Tickets' }).click();

    await searchTickets(page, 'Payment gateway');
    await waitForTicketList(page);
    await expectTicketRowVisible(page, ticketTitle(runId, 'Payment gateway failure'));
    await expectTicketRowHidden(page, ticketTitle(runId, 'LOGIN timeout'));
  });

  test('searches by description keyword', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Tickets' }).click();

    await searchTickets(page, 'checkout payment');
    await waitForTicketList(page);
    await expectTicketRowVisible(page, ticketTitle(runId, 'Payment gateway failure'));
  });

  test('searches case-insensitively', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Tickets' }).click();

    await searchTickets(page, 'login');
    await waitForTicketList(page);
    await expectTicketRowVisible(page, ticketTitle(runId, 'LOGIN timeout'));

    await searchTickets(page, 'LOGIN');
    await waitForTicketList(page);
    await expectTicketRowVisible(page, ticketTitle(runId, 'LOGIN timeout'));
  });

  test('filters tickets by status', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Tickets' }).click();

    await filterTicketsByStatus(page, 'IN_PROGRESS');
    await waitForTicketList(page);
    await expectTicketRowVisible(page, ticketTitle(runId, 'LOGIN timeout'));
    await expectTicketRowHidden(page, ticketTitle(runId, 'Payment gateway failure'));
  });

  test('applies combined keyword and status filters', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Tickets' }).click();

    await searchTickets(page, 'billing');
    await filterTicketsByStatus(page, 'RESOLVED');
    await waitForTicketList(page);

    await expectTicketRowVisible(page, ticketTitle(runId, 'Closed billing issue'));
    await expectTicketRowHidden(page, ticketTitle(runId, 'Payment gateway failure'));
  });
});
