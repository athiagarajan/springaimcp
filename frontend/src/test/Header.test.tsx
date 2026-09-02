import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { Header } from '../components/Header';

describe('Header Component', () => {
  it('renders application branding and status badges', () => {
    render(<Header />);

    expect(screen.getByText('Indian Temples Explorer')).toBeInTheDocument();
    expect(screen.getByText('templeinfo (96 temples)')).toBeInTheDocument();
    expect(screen.getByText('Basic Auth Secured')).toBeInTheDocument();
    expect(screen.getByText('Protected Swagger UI')).toBeInTheDocument();
  });
});
