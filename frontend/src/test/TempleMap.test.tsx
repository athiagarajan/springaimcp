import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { TempleMap } from '../components/TempleMap';
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
  oldYear: null,
  historicalName: null,
  city: 'Palani',
  district: 'Dindigul',
  state: 'Tamil Nadu',
  singers: null,
  festival: null,
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

describe('TempleMap Component', () => {
  it('renders map container and active GPS location badges', () => {
    const handleSelect = vi.fn();
    render(<TempleMap temples={[sampleTemple]} onSelectTemple={handleSelect} />);

    expect(screen.getByText('Interactive Temple Locations Map')).toBeInTheDocument();
    expect(screen.getByText(/1 Map Pins/i)).toBeInTheDocument();
  });
});
