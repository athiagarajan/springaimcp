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

(global as any).EventSource = MockEventSource;
window.scrollTo = vi.fn();
Element.prototype.scrollIntoView = vi.fn();
