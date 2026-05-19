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
const isLocalDevHost = ['localhost', '127.0.0.1', '::1'].includes(window.location.hostname);
const localApiUrl = '';
const localAuthUrl = '/auth';

export const APP_CONFIG = {
  apiUrl: isLocalDevHost ? localApiUrl : trimTrailingSlash(rawApiUrl),
  authUrl: isLocalDevHost ? localAuthUrl : trimTrailingSlash(rawAuthUrl)
};
