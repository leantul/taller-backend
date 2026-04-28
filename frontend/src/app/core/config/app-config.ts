declare global {
  interface Window {
    __env?: {
      API_URL?: string;
      AUTH_URL?: string;
    };
  }
}

const defaultApiUrl = 'http://localhost:8080';

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}

const rawApiUrl = window.__env?.API_URL || defaultApiUrl;
const rawAuthUrl = window.__env?.AUTH_URL || `${trimTrailingSlash(rawApiUrl)}/auth`;

export const APP_CONFIG = {
  apiUrl: trimTrailingSlash(rawApiUrl),
  authUrl: trimTrailingSlash(rawAuthUrl)
};
