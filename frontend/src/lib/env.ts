export function getApiBaseUrl(): string {
  const baseUrl = import.meta.env.VITE_API_BASE_URL;

  if (!baseUrl) {
    throw new Error('VITE_API_BASE_URL is not configured');
  }

  return baseUrl.replace(/\/$/, '');
}
