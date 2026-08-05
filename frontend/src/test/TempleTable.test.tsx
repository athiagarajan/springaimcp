import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { TempleTable } from '../components/TempleTable';
import { Temple } from '../types/temple';

const sampleTemple: Temple = {
  id: 494,
  name: 'sri Idumban temple',
  moolavar: 'Idumban',
  urchavar: null,
  ammanThayar: null,
  thalaVirutcham: null,
  theertham: null,
  agamamPooja: null,
  oldYear: '1000 AD',
  historicalName: null,
  city: 'Palani',
  district: 'Dindigul',
  state: 'Tamil Nadu',
  singers: null,
  festival: 'Thaipusam',
  generalInformation: null,
  address: null,
  phone: null,
  openingTime: null,
  speciality: null,
  prayers: null,
  thanksGiving: null,
  greatness: null,
  history: null,
  features: null,
  hfLat: 10.4413,
  hfLan: 77.5275,
  location: null,
  nearByAirport: null,
  nearByRailwayStation: null,
  accommodation: null,
};

describe('TempleTable Component', () => {
  it('renders loading spinner when isLoading is true', () => {
    render(<TempleTable temples={[]} onSelectTemple={vi.fn()} isLoading={true} />);
    expect(screen.getByText('Loading temple data...')).toBeInTheDocument();
  });

  it('renders temple cards and handles card click and details button click', () => {
    const handleSelect = vi.fn();
    render(<TempleTable temples={[sampleTemple]} onSelectTemple={handleSelect} isLoading={false} />);

    expect(screen.getByText('sri Idumban temple')).toBeInTheDocument();

    const card = screen.getByText('sri Idumban temple');
    fireEvent.click(card);
    expect(handleSelect).toHaveBeenCalledWith(sampleTemple);

    const detailsBtn = screen.getByText('Details');
    fireEvent.click(detailsBtn);
    expect(handleSelect).toHaveBeenCalledWith(sampleTemple);
  });

  it('filters temples by query string', () => {
    render(<TempleTable temples={[sampleTemple]} onSelectTemple={vi.fn()} isLoading={false} />);

    const filterInput = screen.getByPlaceholderText(/Quick filter results/i);
    fireEvent.change(filterInput, { target: { value: 'NonExistent' } });

    expect(screen.getByText(/No Temple Records Displayed/i)).toBeInTheDocument();
  });
});
