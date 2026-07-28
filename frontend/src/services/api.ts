import { Temple } from '../types/temple';

export async function fetchAllTemples(): Promise<Temple[]> {
  const res = await fetch('/api/v1/temples');
  if (!res.ok) throw new Error('Failed to fetch temples');
  return res.json();
}

export async function searchTemples(params: { state?: string; district?: string; city?: string; keyword?: string }): Promise<Temple[]> {
  const query = new URLSearchParams();
  if (params.state) query.append('state', params.state);
  if (params.district) query.append('district', params.district);
  if (params.city) query.append('city', params.city);
  if (params.keyword) query.append('keyword', params.keyword);

  const res = await fetch(`/api/v1/temples/search?${query.toString()}`);
  if (!res.ok) throw new Error('Search failed');
  return res.json();
}

export function streamDynamicQuery(prompt: string, onChunk: (chunk: string) => void, onDone: () => void, onError: (err: any) => void) {
  const url = `/api/v1/temples/stream/query?prompt=${encodeURIComponent(prompt)}`;
  const eventSource = new EventSource(url);

  eventSource.onmessage = (event) => {
    onChunk(event.data);
  };

  eventSource.onerror = (err) => {
    eventSource.close();
    onError(err);
  };

  return () => {
    eventSource.close();
    onDone();
  };
}
