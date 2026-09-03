import { render, screen, fireEvent, act } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import App from '../App';

vi.mock('../services/api', () => ({
  fetchAllTemples: vi.fn().mockResolvedValue([
    {
      id: 494,
      name: 'Sri Idumban Temple',
      moolavar: 'Idumban',
      city: 'Palani',
      district: 'Dindigul',
      state: 'Tamil Nadu',
      speciality: 'Special Temple',
      history: 'Historical Info',
      hfLat: 10.44,
      hfLan: 77.52,
    },
  ]),
  searchTemples: vi.fn().mockResolvedValue([
    {
      id: 494,
      name: 'Sri Idumban Temple',
      moolavar: 'Idumban',
      city: 'Palani',
      district: 'Dindigul',
      state: 'Tamil Nadu',
      speciality: 'Special Temple',
      history: 'Historical Info',
      hfLat: 10.44,
      hfLan: 77.52,
    },
  ]),
  streamDynamicQuery: vi.fn((_prompt, onChunk, onComplete) => {
    onChunk('Streaming data...');
    onComplete();
  }),
  fetchTempleImages: vi.fn().mockResolvedValue([]),
}));

describe('App Component', () => {
  it('renders Header, PromptBar, Map, Table, and handles full flow', async () => {
    await act(async () => {
      render(<App />);
    });

    expect(screen.getByText('Indian Temples Explorer')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ask anything about the temples database/i)).toBeInTheDocument();

    const input = screen.getByPlaceholderText(/Ask anything about the temples database/i);
    const askBtn = screen.getByRole('button', { name: /Ask AI/i });

    await act(async () => {
      fireEvent.change(input, { target: { value: 'Palani' } });
      fireEvent.click(askBtn);
    });

    const card = screen.getByText('Sri Idumban Temple');
    await act(async () => {
      fireEvent.click(card);
    });

    expect(screen.getByText('Special Temple')).toBeInTheDocument();

    const closeBtn = screen.getByText('Close Details');
    await act(async () => {
      fireEvent.click(closeBtn);
    });
  });

  it('hides streaming log by default and toggles visibility when button is clicked', async () => {
    await act(async () => {
      render(<App />);
    });

    // Hidden by default
    expect(screen.queryByText('Spring AI Streaming Reasoning Log')).not.toBeInTheDocument();
    const toggleBtn = screen.getByRole('button', { name: /Show Streaming Log/i });
    expect(toggleBtn).toBeInTheDocument();

    // Click to show
    await act(async () => {
      fireEvent.click(toggleBtn);
    });
    expect(screen.getByText('Spring AI Streaming Reasoning Log')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Hide Streaming Log/i })).toBeInTheDocument();

    // Click to hide again
    const hideBtn = screen.getByRole('button', { name: /Hide Streaming Log/i });
    await act(async () => {
      fireEvent.click(hideBtn);
    });
    expect(screen.queryByText('Spring AI Streaming Reasoning Log')).not.toBeInTheDocument();
  });
});
