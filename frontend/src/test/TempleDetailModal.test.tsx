import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { TempleDetailModal } from '../components/TempleDetailModal';
import { Temple } from '../types/temple';

const sampleTemple: Temple = {
  id: 494,
  name: 'sri Idumban temple',
  moolavar: 'Idumban',
  urchavar: 'Idumban Urchavar',
  ammanThayar: 'Amman',
  thalaVirutcham: 'Neem Tree',
  theertham: 'Palani Springs',
  agamamPooja: 'Agamam',
  oldYear: '1000 AD',
  historicalName: 'Old Palani Temple',
  city: 'Palani',
  district: 'Dindigul',
  state: 'Tamil Nadu',
  singers: 'Tamil Saints',
  festival: 'Thaipusam',
  generalInformation: 'Sacred Hill Temple',
  address: 'Palani Hill Road',
  phone: '04545-242293',
  openingTime: '6:00 AM - 8:00 PM',
  speciality: 'Dedicated to Idumban and Lord Murugan',
  prayers: 'Health and Prosperity',
  thanksGiving: 'Kavadi Offering',
  greatness: 'Great Spiritual Center',
  history: 'Ancient historical significance dating back centuries',
  features: 'Hill steps, Win transport',
  hfLat: 10.4413,
  hfLan: 77.5275,
  location: 'Hill Top',
  nearByAirport: 'Madurai Airport (IXM)',
  nearByRailwayStation: 'Palani Junction (PLNI)',
  accommodation: 'Palani Devasthanam Cottages',
};

describe('TempleDetailModal Component', () => {
  it('returns null when temple is null', () => {
    const { container } = render(<TempleDetailModal temple={null} onClose={vi.fn()} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders all temple details and handles close button click', () => {
    const handleClose = vi.fn();
    render(<TempleDetailModal temple={sampleTemple} onClose={handleClose} />);

    expect(screen.getByText('sri Idumban temple')).toBeInTheDocument();
    expect(screen.getByText(/Old Palani Temple/i)).toBeInTheDocument();
    expect(screen.getByText(/Palani Hill Road/i)).toBeInTheDocument();
    expect(screen.getByText(/Palani Junction \(PLNI\)/i)).toBeInTheDocument();

    const closeBtn = screen.getByText('Close Details');
    fireEvent.click(closeBtn);

    expect(handleClose).toHaveBeenCalled();
  });
});
