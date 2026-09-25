import { expect, type Page } from '@playwright/test';
import type { CreateTicketPayload } from './api-client';

export async function openApplication(page: Page): Promise<void> {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Support Ticket Management' })).toBeVisible();
}

export async function createTicketViaUi(
  page: Page,
  payload: CreateTicketPayload,
): Promise<void> {
  await page.getByRole('link', { name: 'Create Ticket' }).click();
  await expect(page.getByRole('heading', { name: 'Create Ticket' })).toBeVisible();

  await page.locator('#ticket-title').fill(payload.title);
  await page.locator('#ticket-description').fill(payload.description);
  await page.locator('#ticket-priority').selectOption(payload.priority);
  await page.locator('#ticket-assignee').fill(payload.assignee);
  await page.getByRole('button', { name: 'Create Ticket' }).click();
}

export async function expectTicketDetails(
  page: Page,
  values: {
    title: string;
    description: string;
    priority: string;
    status: string;
    assignee: string;
  },
): Promise<void> {
  const detailsMeta = page.locator('.ticket-details-view .ticket-details-meta, .ticket-edit-form .ticket-details-meta');
  await expect(detailsMeta.getByText(values.title, { exact: true })).toBeVisible();
  await expect(page.getByText(values.description, { exact: true })).toBeVisible();
  await expect(detailsMeta.getByText(values.priority, { exact: true })).toBeVisible();
  await expect(detailsMeta.getByText(values.assignee, { exact: true })).toBeVisible();

  const statusSection = page.getByRole('heading', { name: 'Status Actions' }).locator('..');
  await expect(statusSection.getByText(values.status, { exact: true })).toBeVisible();
}

export async function expectTicketRowVisible(page: Page, title: string): Promise<void> {
  await expect(page.locator('tbody tr', { hasText: title })).toBeVisible();
}

export async function expectTicketRowHidden(page: Page, title: string): Promise<void> {
  await expect(page.locator('tbody tr', { hasText: title })).toHaveCount(0);
}

export async function waitForTicketList(page: Page): Promise<void> {
  await expect(page.getByText('Loading tickets...')).not.toBeVisible();
}

export async function editTicketViaUi(
  page: Page,
  values: {
    title: string;
    description: string;
    priority: string;
    assignee: string;
  },
): Promise<void> {
  await page.getByRole('button', { name: 'Edit Ticket' }).click();
  await page.locator('#edit-ticket-title').fill(values.title);
  await page.locator('#edit-ticket-description').fill(values.description);
  await page.locator('#edit-ticket-priority').selectOption(values.priority);
  await page.locator('#edit-ticket-assignee').fill(values.assignee);
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByText('Ticket updated successfully.')).toBeVisible();
}

export async function addCommentViaUi(page: Page, content: string): Promise<void> {
  await page.locator('#comment-content').fill(content);
  await page.getByRole('button', { name: 'Add Comment' }).click();
  await expect(page.getByText(content)).toBeVisible();
}

export async function transitionStatusViaUi(page: Page, actionLabel: string): Promise<void> {
  await page.getByRole('button', { name: actionLabel }).click();
}

export async function acceptConfirmationDialogs(page: Page): Promise<void> {
  page.on('dialog', async (dialog) => {
    await dialog.accept();
  });
}

export async function searchTickets(page: Page, keyword: string): Promise<void> {
  await page.locator('#ticket-search').fill(keyword);
  await page.getByRole('button', { name: 'Search' }).click();
}

export async function filterTicketsByStatus(page: Page, status: string): Promise<void> {
  await page.locator('#ticket-status').selectOption(status);
}
