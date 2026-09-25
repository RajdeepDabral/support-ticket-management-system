import { expect, test } from '@playwright/test';
import { openApplication } from './helpers/ui-actions';

test.describe('Not found handling', () => {
  test('shows a meaningful message for unknown ticket details', async ({ page }) => {
    await openApplication(page);
    await page.goto('/tickets/999999999');

    await expect(page.getByText('Ticket not found.')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Return to ticket list' })).toBeVisible();
  });

  test('returns 404 for unknown ticket via API', async ({ request }) => {
    const response = await request.get(
      `${process.env.E2E_API_BASE_URL ?? 'http://localhost:8080/api/v1'}/tickets/999999999`,
    );

    expect(response.status()).toBe(404);
    const body = await response.json();
    expect(body.code).toBe('TICKET_NOT_FOUND');
  });
});
