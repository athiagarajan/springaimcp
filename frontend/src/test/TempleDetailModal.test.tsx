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
    vi.spyOn(api, 'fetchTempleImages').mockResolvedValue([
      {
        url: 'https://upload.wikimedia.org/test-photo.jpg',
        title: 'Ekambareswarar Gopuram',
        description: 'Main Dravidian Gopuram',
        source: 'Wikipedia',
      },
    ]);
  });

  it('returns null when temple is null', () => {
    const { container } = render(<TempleDetailModal temple={null} onClose={vi.fn()} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders all temple details and handles close button click', () => {
    const handleClose = vi.fn();
    render(<TempleDetailModal temple={sampleTemple} onClose={handleClose} />);

    expect(screen.getByText('sri Idumban temple')).toBeInTheDocument();
    expect(screen.getByText('Palani, Dindigul, Tamil Nadu')).toBeInTheDocument();

    const closeBtn = screen.getByRole('button', { name: 'Close Details' });
    fireEvent.click(closeBtn);
    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('renders authentic temple photograph carousel and opens lightbox on click', async () => {
    render(<TempleDetailModal temple={sampleTemple} onClose={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Ekambareswarar Gopuram')).toBeInTheDocument();
      expect(screen.getByText('Wikipedia')).toBeInTheDocument();
    });

    const img = screen.getByAltText('Ekambareswarar Gopuram');
    expect(img).toBeInTheDocument();

    // Click image to open fullscreen lightbox
    fireEvent.click(img);

    await waitFor(() => {
      expect(screen.getByTitle('Close Fullscreen View')).toBeInTheDocument();
    });

    // Close lightbox
    fireEvent.click(screen.getByTitle('Close Fullscreen View'));
    expect(screen.queryByTitle('Close Fullscreen View')).not.toBeInTheDocument();
  });

  it('translates to Tamil when clicking Tamil in the combo button, and switches back to English', async () => {
    const fetchSpy = vi.spyOn(api, 'fetchTempleTranslation').mockResolvedValue(tamilTemple);

    render(<TempleDetailModal temple={sampleTemple} onClose={vi.fn()} />);

    // Click combo button to translate to Tamil
    const comboBtn = screen.getByRole('button', { name: 'Toggle language' });
    expect(comboBtn).toHaveTextContent('தமிழ் (Tamil)');
    fireEvent.click(comboBtn);

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledWith(494, 'ta');
      expect(screen.getByText('ஸ்ரீ இடும்பன் கோயில்')).toBeInTheDocument();
      expect(comboBtn).toHaveTextContent('English');
    });

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
