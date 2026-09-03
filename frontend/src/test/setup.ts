import '@testing-library/jest-dom';
import { vi } from 'vitest';

class MockEventSource {
  onmessage: ((event: any) => void) | null = null;
  onerror: ((event: any) => void) | null = null;
  url: string;

  constructor(url: string) {
    this.url = url;
  }

  close() {}
}

(globalThis as any).EventSource = MockEventSource;

class MockResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}
(globalThis as any).ResizeObserver = MockResizeObserver;

window.scrollTo = vi.fn();
Element.prototype.scrollIntoView = vi.fn();
