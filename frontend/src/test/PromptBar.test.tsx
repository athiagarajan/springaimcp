import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { PromptBar } from '../components/PromptBar';

describe('PromptBar Component', () => {
  it('renders input field and handles submit', () => {
    const handleSearch = vi.fn();
    render(<PromptBar onSearch={handleSearch} onReset={vi.fn()} isLoading={false} />);

    const input = screen.getByPlaceholderText(/Ask anything about the temples database/i);
    expect(input).toBeInTheDocument();

    fireEvent.change(input, { target: { value: 'Temples in Palani' } });
    const submitBtn = screen.getByRole('button', { name: /Ask AI/i });
    fireEvent.click(submitBtn);

    expect(handleSearch).toHaveBeenCalledWith('Temples in Palani');
  });

  it('handles empty input submit without calling onSearch', () => {
    const handleSearch = vi.fn();
    render(<PromptBar onSearch={handleSearch} onReset={vi.fn()} isLoading={false} />);

    const submitBtn = screen.getByRole('button', { name: /Ask AI/i });
    fireEvent.click(submitBtn);

    expect(handleSearch).not.toHaveBeenCalled();
  });

  it('displays loading state spinner during stream execution', () => {
    render(<PromptBar onSearch={vi.fn()} onReset={vi.fn()} isLoading={true} />);
    expect(screen.getByText('Streaming...')).toBeInTheDocument();
  });

  it('triggers search when preset suggestion is clicked', () => {
    const handleSearch = vi.fn();
    render(<PromptBar onSearch={handleSearch} onReset={vi.fn()} isLoading={false} />);

    const presetBtn = screen.getByText(/Find temples in Dindigul or Palani/i);
    fireEvent.click(presetBtn);

    expect(handleSearch).toHaveBeenCalledWith('Find temples in Dindigul or Palani dedicated to Lord Shiva or Idumban');
  });

  it('triggers onReset when Reset Search & Pins button is clicked', () => {
    const handleReset = vi.fn();
    render(<PromptBar onSearch={vi.fn()} onReset={handleReset} isLoading={false} />);

    const resetBtn = screen.getByRole('button', { name: /Reset Search & Pins/i });
    fireEvent.click(resetBtn);

    expect(handleReset).toHaveBeenCalledTimes(1);
  });

  it('renders toggle button with Show Streaming Log by default and triggers onToggleStreamLog', () => {
    const handleToggle = vi.fn();
    render(
      <PromptBar
        onSearch={vi.fn()}
        onReset={vi.fn()}
        isLoading={false}
        showStreamLog={false}
        onToggleStreamLog={handleToggle}
      />
    );

    const toggleBtn = screen.getByRole('button', { name: /Show Streaming Log/i });
    expect(toggleBtn).toBeInTheDocument();
    fireEvent.click(toggleBtn);
    expect(handleToggle).toHaveBeenCalledTimes(1);
  });

  it('renders toggle button with Hide Streaming Log when showStreamLog is true', () => {
    const handleToggle = vi.fn();
    render(
      <PromptBar
        onSearch={vi.fn()}
        onReset={vi.fn()}
        isLoading={false}
        showStreamLog={true}
        onToggleStreamLog={handleToggle}
      />
    );

    const toggleBtn = screen.getByRole('button', { name: /Hide Streaming Log/i });
    expect(toggleBtn).toBeInTheDocument();
  });
});
