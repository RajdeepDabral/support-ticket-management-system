import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config';

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      include: ['src/**/*.test.{ts,tsx}'],
      exclude: ['e2e/**', 'node_modules/**'],
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/test/setup.ts',
      env: {
        VITE_API_BASE_URL: 'http://localhost:8080/api/v1',
      },
    },
  }),
);
