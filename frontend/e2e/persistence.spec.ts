import { execSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { expect, test } from '@playwright/test';
import {
  addComment,
  createTicket,
  getTicket,
  transitionTicketStatus,
  updateTicket,
  waitForBackend,
} from './helpers/api-client';
import { createTestRunId, ticketTitle } from './helpers/test-data';
import { expectTicketDetails, openApplication } from './helpers/ui-actions';

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const composeFile = path.resolve(currentDir, '../../docker-compose.yml');

function dockerAvailable(): boolean {
  try {
    execSync('docker compose version', { stdio: 'ignore' });
    return true;
  } catch {
    return false;
  }
}

function restartBackendContainer(): void {
  execSync(`docker compose -f "${composeFile}" restart backend`, {
    stdio: 'inherit',
  });
}

test.describe('Persistence after backend restart', () => {
  test.skip(!dockerAvailable(), 'Docker is required to restart the backend container.');

  test('retains ticket updates, comments, and status after backend restart', async ({ page, request }) => {
    const runId = createTestRunId();
    const initialTitle = ticketTitle(runId, 'Persistence ticket');
    const updatedTitle = ticketTitle(runId, 'Persistence ticket updated');
    const commentText = `${runId} persisted comment after restart`;

    const created = await createTicket(request, {
      title: initialTitle,
      description: 'Initial persistence description.',
      priority: 'HIGH',
      assignee: 'persist.tester',
    });

    await updateTicket(request, created.id, {
      title: updatedTitle,
      description: 'Updated persistence description.',
      priority: 'CRITICAL',
      assignee: 'persist.reviewer',
    });

    await addComment(request, created.id, commentText, 'persist.reviewer');
    await transitionTicketStatus(request, created.id, 'IN_PROGRESS');

    restartBackendContainer();
    await waitForBackend(request);

    const persisted = await getTicket(request, created.id);
    expect(persisted.title).toBe(updatedTitle);
    expect(persisted.description).toBe('Updated persistence description.');
    expect(persisted.priority).toBe('CRITICAL');
    expect(persisted.assignee).toBe('persist.reviewer');
    expect(persisted.status).toBe('IN_PROGRESS');
    expect(persisted.comments).toHaveLength(1);
    expect(persisted.comments[0].content).toBe(commentText);
    expect(persisted.comments[0].author).toBe('persist.reviewer');

    await openApplication(page);
    await page.goto(`/tickets/${created.id}`);

    await expectTicketDetails(page, {
      title: updatedTitle,
      description: 'Updated persistence description.',
      priority: 'CRITICAL',
      status: 'IN_PROGRESS',
      assignee: 'persist.reviewer',
    });
    await expect(page.getByText(commentText)).toBeVisible();
  });
});
