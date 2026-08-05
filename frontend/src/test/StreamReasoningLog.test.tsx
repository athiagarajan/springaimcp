import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { StreamReasoningLog } from '../components/StreamReasoningLog';

describe('StreamReasoningLog Component', () => {
  it('renders empty prompt placeholder when no log content exists', () => {
    render(<StreamReasoningLog streamContent="" isStreaming={false} />);
    expect(screen.getByText(/Enter a prompt above to view real-time streaming/i)).toBeInTheDocument();
  });

  it('renders streaming content and live SSE indicator', () => {
    render(<StreamReasoningLog streamContent="SELECT * FROM temples WHERE city='Palani';" isStreaming={true} />);
    expect(screen.getByText(/SELECT \* FROM temples WHERE city='Palani';/i)).toBeInTheDocument();
    expect(screen.getByText(/LIVE SSE/i)).toBeInTheDocument();
  });

  it('handles copy log button click', () => {
    Object.assign(navigator, {
      clipboard: {
        writeText: vi.fn(),
      },
    });

    render(<StreamReasoningLog streamContent="Test Log Content" isStreaming={false} />);
    const copyBtn = screen.getByText(/Copy Log/i);
    fireEvent.click(copyBtn);
    expect(screen.getByText(/Copied/i)).toBeInTheDocument();
  });
});
