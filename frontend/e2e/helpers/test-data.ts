export function createTestRunId(): string {
  return `e2e-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

export function ticketTitle(runId: string, label: string): string {
  return `${runId} ${label}`;
}
