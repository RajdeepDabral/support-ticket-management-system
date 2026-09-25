import { expect, test } from '@playwright/test';
import { createTicket } from './helpers/api-client';
import { createTestRunId, ticketTitle } from './helpers/test-data';
import { openApplication } from './helpers/ui-actions';

test.describe('Validation and meaningful UI errors', () => {
  test('rejects blank title on create ticket form', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Create Ticket' }).click();

    await page.locator('#ticket-description').fill('Description without title.');
    await page.locator('#ticket-priority').selectOption('HIGH');
    await page.locator('#ticket-assignee').fill('validation.tester');
    await page.getByRole('button', { name: 'Create Ticket' }).click();

    await expect(page.getByText('Title is required.')).toBeVisible();
  });

  test('rejects blank description on create ticket form', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Create Ticket' }).click();

    await page.locator('#ticket-title').fill('Title without description');
    await page.locator('#ticket-priority').selectOption('HIGH');
    await page.locator('#ticket-assignee').fill('validation.tester');
    await page.getByRole('button', { name: 'Create Ticket' }).click();

    await expect(page.getByText('Description is required.')).toBeVisible();
  });

  test('rejects missing priority on create ticket form', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Create Ticket' }).click();

    await page.locator('#ticket-title').fill('Missing priority ticket');
    await page.locator('#ticket-description').fill('No priority selected.');
    await page.locator('#ticket-assignee').fill('validation.tester');
    await page.getByRole('button', { name: 'Create Ticket' }).click();

    await expect(page.getByText('Priority is required.')).toBeVisible();
  });

  test('rejects blank assignee on create ticket form', async ({ page }) => {
    await openApplication(page);
    await page.getByRole('link', { name: 'Create Ticket' }).click();

    await page.locator('#ticket-title').fill('Missing assignee ticket');
    await page.locator('#ticket-description').fill('No assignee provided.');
    await page.locator('#ticket-priority').selectOption('LOW');
    await page.getByRole('button', { name: 'Create Ticket' }).click();

    await expect(page.getByText('Assignee is required.')).toBeVisible();
  });

  test('rejects invalid priority from backend API', async ({ request }) => {
    const response = await request.post(`${process.env.E2E_API_BASE_URL ?? 'http://localhost:8080/api/v1'}/tickets`, {
      data: {
        title: 'Invalid priority ticket',
        description: 'Priority value is not supported.',
        priority: 'UNKNOWN',
        assignee: 'validation.tester',
      },
    });

    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.code).toBeTruthy();
    expect(body.message).toBeTruthy();
  });

  test('rejects blank and whitespace-only comments', async ({ page, request }) => {
    const runId = createTestRunId();
    const ticket = await createTicket(request, {
      title: ticketTitle(runId, 'Comment validation'),
      description: 'Ticket for comment validation checks.',
      priority: 'MEDIUM',
      assignee: 'validation.tester',
    });

    await openApplication(page);
    await page.goto(`/tickets/${ticket.id}`);

    await page.getByRole('button', { name: 'Add Comment' }).click();
    await expect(page.getByText('Comment cannot be empty.')).toBeVisible();

    await page.locator('#comment-content').fill('   ');
    await page.getByRole('button', { name: 'Add Comment' }).click();
    await expect(page.getByText('Comment cannot be empty.')).toBeVisible();
  });
});
