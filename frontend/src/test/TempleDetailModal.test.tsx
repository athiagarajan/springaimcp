import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TempleDetailModal } from '../components/TempleDetailModal';
import { Temple } from '../types/temple';
import * as api from '../services/api';

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

const tamilTemple: Temple = {
  ...sampleTemple,
  name: 'ஸ்ரீ இடும்பன் கோயில்',
  moolavar: 'இடும்பன்',
};

const teluguTemple: Temple = {
  ...sampleTemple,
  name: 'శ్రీ ఇడుంబన్ ఆలయం',
  moolavar: 'ఇడుంబన్',
};

describe('TempleDetailModal Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

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

  it('translates to Tamil when clicking Tamil in the combo button, and switches back to English', async () => {
    const fetchSpy = vi.spyOn(api, 'fetchTempleTranslation').mockResolvedValue(tamilTemple);

    render(<TempleDetailModal temple={sampleTemple} onClose={vi.fn()} />);

    // Initially in English
    expect(screen.getByText('sri Idumban temple')).toBeInTheDocument();

    // The combo button starts showing 'தமிழ் (Tamil)' by default
    const comboBtn = screen.getByRole('button', { name: 'Toggle language' });
    expect(comboBtn).toHaveTextContent('தமிழ் (Tamil)');

    // Click combo button to translate to Tamil
    fireEvent.click(comboBtn);

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledWith(494, 'ta');
      expect(screen.getByText('ஸ்ரீ இடும்பன் கோயில்')).toBeInTheDocument();
    });

    // Now combo button shows 'English'
    expect(comboBtn).toHaveTextContent('English');

    // Click combo button again to switch back to English
    fireEvent.click(comboBtn);

    expect(screen.getByText('sri Idumban temple')).toBeInTheDocument();
    expect(comboBtn).toHaveTextContent('தமிழ் (Tamil)');
  });

  it('allows opening dropdown and selecting Telugu', async () => {
    const fetchSpy = vi.spyOn(api, 'fetchTempleTranslation').mockResolvedValue(teluguTemple);

    render(<TempleDetailModal temple={sampleTemple} onClose={vi.fn()} />);

    // Open dropdown
    const chevronBtn = screen.getByRole('button', { name: 'Open language selection menu' });
    fireEvent.click(chevronBtn);

    // Click Telugu option
    const teluguOpt = screen.getByRole('option', { name: /తెలుగు \(Telugu\)/i });
    fireEvent.click(teluguOpt);

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledWith(494, 'te');
      expect(screen.getByText('శ్రీ ఇడుంబన్ ఆలయం')).toBeInTheDocument();
    });
  });
});
