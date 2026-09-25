const HEALTH_URL = process.env.E2E_HEALTH_URL ?? 'http://localhost:8080/actuator/health';
const MAX_ATTEMPTS = 60;
const RETRY_DELAY_MS = 1_000;

async function waitForBackend(): Promise<void> {
  for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt += 1) {
    try {
      const response = await fetch(HEALTH_URL);
      if (response.ok) {
        const body = (await response.json()) as { status?: string };
        if (body.status === 'UP') {
          return;
        }
      }
    } catch {
      // Backend not ready yet.
    }

    await new Promise((resolve) => {
      setTimeout(resolve, RETRY_DELAY_MS);
    });
  }

  throw new Error(
    `Backend is not available at ${HEALTH_URL}. Start it with: docker compose up -d`,
  );
}

export default async function globalSetup(): Promise<void> {
  await waitForBackend();
}
