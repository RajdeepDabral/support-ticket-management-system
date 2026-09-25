import { defineConfig, devices } from '@playwright/test';

const apiBaseUrl = process.env.E2E_API_BASE_URL ?? 'http://localhost:8080/api/v1';
const frontendPort = process.env.E2E_FRONTEND_PORT ?? '5173';
const frontendBaseUrl = process.env.E2E_BASE_URL ?? `http://localhost:${frontendPort}`;

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  use: {
    baseURL: frontendBaseUrl,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  globalSetup: './e2e/global-setup.ts',
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: frontendBaseUrl,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: {
      VITE_API_BASE_URL: apiBaseUrl,
    },
  },
});
