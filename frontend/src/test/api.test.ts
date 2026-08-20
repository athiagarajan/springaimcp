import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchAllTemples, streamDynamicQuery } from '../services/api';

describe('API Services', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('fetchAllTemples fetches temple records with basic auth headers', async () => {
    const mockTemples = [{ id: 494, name: 'Sri Idumban Temple' }];
    (globalThis as any).fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockTemples,
    } as any);

    const result = await fetchAllTemples();
    expect(result).toEqual(mockTemples);
  });

  it('fetchAllTemples throws error on HTTP error response', async () => {
    (globalThis as any).fetch = vi.fn().mockResolvedValue({
      ok: false,
    } as any);

    await expect(fetchAllTemples()).rejects.toThrow('Failed to fetch temples');
  });

  it('streamDynamicQuery processes chunk events', () => {
    const onChunk = vi.fn();
    const onComplete = vi.fn();
    const onError = vi.fn();

    const mockEs: any = { close: vi.fn(), addEventListener: vi.fn() };
    vi.stubGlobal('EventSource', vi.fn().mockImplementation(() => mockEs));

    streamDynamicQuery('Palani temples', onChunk, onComplete, onError);

    mockEs.onmessage({ data: JSON.stringify({ text: 'Sample Chunk' }) });
    expect(onChunk).toHaveBeenCalledWith('Sample Chunk');
  });

  it('streamDynamicQuery processes completion event [DONE]', () => {
    const onChunk = vi.fn();
    const onComplete = vi.fn();
    const onError = vi.fn();

    const mockEs: any = { close: vi.fn(), addEventListener: vi.fn() };
    vi.stubGlobal('EventSource', vi.fn().mockImplementation(() => mockEs));

    streamDynamicQuery('Palani temples', onChunk, onComplete, onError);

    mockEs.onmessage({ data: JSON.stringify({ text: '[DONE]' }) });
    expect(onComplete).toHaveBeenCalled();
    expect(mockEs.close).toHaveBeenCalled();
  });

  it('streamDynamicQuery processes error and cleanup', () => {
    const onChunk = vi.fn();
    const onComplete = vi.fn();
    const onError = vi.fn();

    const mockEs: any = { close: vi.fn(), addEventListener: vi.fn() };
    vi.stubGlobal('EventSource', vi.fn().mockImplementation(() => mockEs));

    const cleanup = streamDynamicQuery('Palani temples', onChunk, onComplete, onError);

    mockEs.onerror(new Error('Connection Failed'));
    expect(onComplete).toHaveBeenCalled();

    cleanup();
    expect(mockEs.close).toHaveBeenCalled();
  });
});
