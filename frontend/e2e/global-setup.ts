const API_BASE_URL = process.env.E2E_API_BASE_URL ?? 'http://localhost:8080/api/v1';
const MAX_ATTEMPTS = 60;
const RETRY_DELAY_MS = 1_000;

async function waitForBackend(): Promise<void> {
  for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt += 1) {
    try {
      const response = await fetch(`${API_BASE_URL}/tickets`);
      if (response.ok) {
        return;
      }
    } catch {
      // Backend not ready yet.
    }

    await new Promise((resolve) => {
      setTimeout(resolve, RETRY_DELAY_MS);
    });
  }

  throw new Error(
    `Backend is not available at ${API_BASE_URL}. Start it with: docker compose up -d`,
  );
}

export default async function globalSetup(): Promise<void> {
  await waitForBackend();
}
