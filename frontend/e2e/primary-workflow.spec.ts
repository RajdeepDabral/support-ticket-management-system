import { expect, test } from '@playwright/test';
import { createTestRunId, ticketTitle } from './helpers/test-data';
import {
  addCommentViaUi,
  createTicketViaUi,
  editTicketViaUi,
  expectTicketDetails,
  openApplication,
  transitionStatusViaUi,
} from './helpers/ui-actions';

test.describe('Primary acceptance workflow', () => {
  test('completes create, edit, comment, and lifecycle transitions', async ({ page }) => {
    const runId = createTestRunId();
    const initialTitle = ticketTitle(runId, 'Login issue');
    const updatedTitle = ticketTitle(runId, 'Login issue updated');
    const commentText = `${runId} investigating authentication failure`;

    await openApplication(page);

    await createTicketViaUi(page, {
      title: initialTitle,
      description: 'User cannot access the application after password reset.',
      priority: 'HIGH',
      assignee: 'john.doe',
    });

    await expect(page).toHaveURL(/\/tickets\/\d+$/);
    await expectTicketDetails(page, {
      title: initialTitle,
      description: 'User cannot access the application after password reset.',
      priority: 'HIGH',
      status: 'OPEN',
      assignee: 'john.doe',
    });

    await editTicketViaUi(page, {
      title: updatedTitle,
      description: 'Updated description after investigation started.',
      priority: 'CRITICAL',
      assignee: 'jane.doe',
    });

    await expectTicketDetails(page, {
      title: updatedTitle,
      description: 'Updated description after investigation started.',
      priority: 'CRITICAL',
      status: 'OPEN',
      assignee: 'jane.doe',
    });

    await addCommentViaUi(page, commentText);

    await transitionStatusViaUi(page, 'Start Progress');
    await expect(page.getByText('Ticket status updated successfully.')).toBeVisible();
    await expectTicketDetails(page, {
      title: updatedTitle,
      description: 'Updated description after investigation started.',
      priority: 'CRITICAL',
      status: 'IN_PROGRESS',
      assignee: 'jane.doe',
    });
    await expect(page.getByRole('button', { name: 'Resolve' })).toBeVisible();

    await transitionStatusViaUi(page, 'Resolve');
    await expectTicketDetails(page, {
      title: updatedTitle,
      description: 'Updated description after investigation started.',
      priority: 'CRITICAL',
      status: 'RESOLVED',
      assignee: 'jane.doe',
    });
    await expect(page.getByRole('button', { name: 'Close' })).toBeVisible();

    await transitionStatusViaUi(page, 'Close');
    await expectTicketDetails(page, {
      title: updatedTitle,
      description: 'Updated description after investigation started.',
      priority: 'CRITICAL',
      status: 'CLOSED',
      assignee: 'jane.doe',
    });

    await expect(page.getByText('No status actions available.')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Start Progress' })).not.toBeVisible();
    await expect(page.getByRole('button', { name: 'Resolve' })).not.toBeVisible();
    await expect(page.getByRole('button', { name: 'Close' })).not.toBeVisible();
  });
});
