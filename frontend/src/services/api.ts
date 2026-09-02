import { Temple } from '../types/temple';

const BASIC_AUTH_HEADER = 'Basic ' + btoa('admin:adminpassword');
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const fetchAllTemples = async (): Promise<Temple[]> => {
  const response = await fetch(`${BASE_URL}/api/v1/temples`, {
    headers: {
      Authorization: BASIC_AUTH_HEADER,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch temples');
  }

  return response.json();
};

export const searchTemples = async (query: string): Promise<Temple[]> => {
  const response = await fetch(`${BASE_URL}/api/v1/temples/search?keyword=${encodeURIComponent(query)}`, {
    headers: {
      Authorization: BASIC_AUTH_HEADER,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error('Failed to search temples');
  }

  return response.json();
};

const translationCache = new Map<string, Temple>();

export const fetchTempleTranslation = async (id: number, targetLang: string = 'ta'): Promise<Temple> => {
  const cacheKey = `${id}_${targetLang.toLowerCase()}`;
  if (translationCache.has(cacheKey)) {
    return translationCache.get(cacheKey)!;
  }

  const response = await fetch(`${BASE_URL}/api/v1/temples/${id}/translate?targetLang=${encodeURIComponent(targetLang)}`, {
    headers: {
      Authorization: BASIC_AUTH_HEADER,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error('Failed to translate temple details');
  }

  const result: Temple = await response.json();
  translationCache.set(cacheKey, result);
  return result;
};

export const streamDynamicQuery = (
  prompt: string,
  onChunk: (chunk: string) => void,
  onComplete: () => void,
  onError: (err: any) => void
) => {
  const url = `${BASE_URL}/api/v1/temples/stream/query?prompt=${encodeURIComponent(prompt)}`;
  const eventSource = new EventSource(url);

  eventSource.onmessage = (event: MessageEvent) => {
    try {
      const data = JSON.parse(event.data);
      const text = data.text || '';
      if (text === '[DONE]') {
        eventSource.close();
        onComplete();
      } else {
        onChunk(text);
      }
    } catch {
      if (event.data === '[DONE]') {
        eventSource.close();
        onComplete();
      } else if (event.data) {
        onChunk(event.data);
      }
    }
  };

  eventSource.addEventListener('complete', () => {
    eventSource.close();
    onComplete();
  });

  eventSource.onerror = (err) => {
    eventSource.close();
    if (onError) onError(err);
    onComplete();
  };

  return () => {
    eventSource.close();
  };
};
