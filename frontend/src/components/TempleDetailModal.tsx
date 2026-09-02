import React, { useState, useEffect, useRef } from 'react';
import { Temple } from '../types/temple';
import { fetchTempleTranslation } from '../services/api';
import { X, MapPin, Calendar, Phone, Clock, Train, Plane, Info, ShieldAlert, Crosshair, Languages, Loader2, ChevronDown, Check } from 'lucide-react';

export type SupportedLanguage = 'en' | 'ta' | 'te' | 'hi';

interface TempleDetailModalProps {
  temple: Temple | null;
  onClose: () => void;
  onSelectTemple?: (temple: Temple) => void;
}

const UI_TEXT: Record<SupportedLanguage, {
  temple: string;
  historicalName: string;
  locateMap: string;
  deitiesWorship: string;
  moolavar: string;
  urchavar: string;
  ammanThayar: string;
  agamamPooja: string;
  sacredSymbols: string;
  thalaVirutcham: string;
  theertham: string;
  singers: string;
  oldYear: string;
  speciality: string;
  history: string;
  phone: string;
  openingTime: string;
  address: string;
  railway: string;
  airport: string;
  accommodation: string;
  locatePinFooter: string;
  closeDetails: string;
  translatingMsg: string;
}> = {
  en: {
    temple: 'Temple',
    historicalName: 'Historical Name:',
    locateMap: 'Click to Locate Pin on Map',
    deitiesWorship: 'Deities & Worship',
    moolavar: 'Moolavar:',
    urchavar: 'Urchavar:',
    ammanThayar: 'Amman / Thayar:',
    agamamPooja: 'Agamam / Pooja:',
    sacredSymbols: 'Sacred Symbols',
    thalaVirutcham: 'Thala Virutcham:',
    theertham: 'Theertham:',
    singers: 'Singers:',
    oldYear: 'Old / Construction Year:',
    speciality: 'Speciality & Importance',
    history: 'Temple History',
    phone: 'Phone:',
    openingTime: 'Opening Timings:',
    address: 'Address:',
    railway: 'Nearest Railway:',
    airport: 'Nearest Airport:',
    accommodation: 'Accommodation:',
    locatePinFooter: 'Locate Pin on Map',
    closeDetails: 'Close Details',
    translatingMsg: 'Translating all temple details via Gemini AI...',
  },
  ta: {
    temple: 'திருக்கோயில்',
    historicalName: 'வரலாற்றுப் பெயர்:',
    locateMap: 'வரைபடத்தில் பார்க்க',
    deitiesWorship: 'மூலவர் & வழிபாடுகள்',
    moolavar: 'மூலவர்:',
    urchavar: 'உற்சவர்:',
    ammanThayar: 'அம்மன் / தாயார்:',
    agamamPooja: 'ஆகமம் / பூஜை:',
    sacredSymbols: 'தல விருட்சம் & புனித குறியீடுகள்',
    thalaVirutcham: 'தல விருட்சம்:',
    theertham: 'தீர்த்தம்:',
    singers: 'பாடியவர்கள்:',
    oldYear: 'பழமை / அமைக்கப்பட்ட ஆண்டு:',
    speciality: 'கோயில் சிறப்புகள் & முக்கியத்துவம்',
    history: 'கோயில் வரலாறு',
    phone: 'தொலைபேசி:',
    openingTime: 'நடை திறக்கும் நேரம்:',
    address: 'முகவரி:',
    railway: 'அருகிலுள்ள ரயில் நிலையம்:',
    airport: 'அருகிலுள்ள விமான நிலையம்:',
    accommodation: 'தங்குமிடம்:',
    locatePinFooter: 'வரைபடத்தில் இருப்பிடத்தைக் காட்டு',
    closeDetails: 'விவரங்களை மூடுக',
    translatingMsg: 'ஜெமினி AI மூலம் கோயிலின் அனைத்து விவரங்களும் தமிழில் மொழிபெயர்க்கப்படுகிறது...',
  },
  te: {
    temple: 'ఆలయం',
    historicalName: 'చారిత్రక పేరు:',
    locateMap: 'మ్యాప్‌లో చూడండి',
    deitiesWorship: 'మూలవిరాట్ & పూజలు',
    moolavar: 'మూలవిరాట్:',
    urchavar: 'ఉత్సవమూర్తి:',
    ammanThayar: 'అమ్మవారు / తాయారు:',
    agamamPooja: 'ఆగమం / పూజ:',
    sacredSymbols: 'పవిత్ర వృక్షం & తీర్థాలు',
    thalaVirutcham: 'స్థల వృక్షం:',
    theertham: 'తీర్థం:',
    singers: 'కీర్తించినవారు:',
    oldYear: 'నిర్మాణ కాలం:',
    speciality: 'ఆలయ విశిష్టత & ప్రాముఖ్యత',
    history: 'స్థల పురాణం & చరిత్ర',
    phone: 'ఫోన్ నంబరు:',
    openingTime: 'దర్శన వేళలు:',
    address: 'చిరునామా:',
    railway: 'సమీప రైల్వే స్టేషన్:',
    airport: 'సమీప విమానాశ్రయం:',
    accommodation: 'వసతి సౌకర్యాలు:',
    locatePinFooter: 'మ్యాప్‌లో ఆలయాన్ని గుర్తించండి',
    closeDetails: 'వివరాలు మూసివేయి',
    translatingMsg: 'జెమిని AI ద్వారా ఆలయ వివరాలన్నీ తెలుగులోకి అనువదించబడుతున్నాయి...',
  },
  hi: {
    temple: 'मंदिर',
    historicalName: 'ऐतिहासिक नाम:',
    locateMap: 'मानचित्र पर देखें',
    deitiesWorship: 'मुख्य देवता एवं पूजा विधि',
    moolavar: 'मूलवर (मुख्य देवता):',
    urchavar: 'उत्सव मूर्ति:',
    ammanThayar: 'अम्मन / देवी:',
    agamamPooja: 'आगम / पूजा:',
    sacredSymbols: 'पवित्र वृक्ष एवं तीर्थ',
    thalaVirutcham: 'थल वृक्ष:',
    theertham: 'पवित्र तीर्थ:',
    singers: 'स्तुतिगान / संत:',
    oldYear: 'प्राचीनता / निर्माण वर्ष:',
    speciality: 'मंदिर की विशेषता एवं महत्व',
    history: 'मंदिर का इतिहास एवं कथा',
    phone: 'दूरभाष (फोन):',
    openingTime: 'दर्शन का समय:',
    address: 'पता:',
    railway: 'निकटतम रेलवे स्टेशन:',
    airport: 'निकटतम हवाई अड्डा:',
    accommodation: 'ठहरने की व्यवस्था:',
    locatePinFooter: 'मानचित्र पर मंदिर देखें',
    closeDetails: 'विवरण बंद करें',
    translatingMsg: 'जेमिनी AI द्वारा मंदिर का संपूर्ण विवरण हिन्दी में अनुवादित किया जा रहा है...',
  },
};

interface LanguageOption {
  code: SupportedLanguage;
  name: string;
  native: string;
}

const ALL_LANGUAGES: LanguageOption[] = [
  { code: 'ta', name: 'Tamil', native: 'தமிழ் (Tamil)' },
  { code: 'te', name: 'Telugu', native: 'తెలుగు (Telugu)' },
  { code: 'hi', name: 'Hindi', native: 'हिन्दी (Hindi)' },
  { code: 'en', name: 'English', native: 'English' },
];

export const TempleDetailModal: React.FC<TempleDetailModalProps> = ({ temple, onClose, onSelectTemple }) => {
  const [displayedLang, setDisplayedLang] = useState<SupportedLanguage>('en');
  const [translationCache, setTranslationCache] = useState<Partial<Record<SupportedLanguage, Temple>>>({});
  const [isTranslating, setIsTranslating] = useState<boolean>(false);
  const [translationError, setTranslationError] = useState<string | null>(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState<boolean>(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // When temple changes, reset to default English display
  useEffect(() => {
    setDisplayedLang('en');
    setTranslationCache({});
    setIsTranslating(false);
    setTranslationError(null);
    setIsDropdownOpen(false);
  }, [temple?.id]);

  // Click outside to close dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (!temple) return null;

  const handleLocateOnMap = () => {
    if (onSelectTemple) {
      onSelectTemple(temple);
    }
    onClose();
  };

  const handleLanguageChange = async (targetLang: SupportedLanguage) => {
    setIsDropdownOpen(false);

    if (targetLang === 'en') {
      setDisplayedLang('en');
      setTranslationError(null);
      return;
    }

    // Check if already cached in component state
    if (translationCache[targetLang]) {
      setDisplayedLang(targetLang);
      setTranslationError(null);
      return;
    }

    // Fetch from backend (which also has backend Caffeine/Map cache)
    setIsTranslating(true);
    setTranslationError(null);
    try {
      const result = await fetchTempleTranslation(temple.id, targetLang);
      setTranslationCache((prev) => ({ ...prev, [targetLang]: result }));
      setDisplayedLang(targetLang);
    } catch (err: any) {
      console.error('Translation error:', err);
      setTranslationError('Unable to translate details right now. Displaying English version.');
    } finally {
      setIsTranslating(false);
    }
  };

  const displayTemple = (displayedLang !== 'en' && translationCache[displayedLang])
    ? translationCache[displayedLang]!
    : temple;

  const t = UI_TEXT[displayedLang] || UI_TEXT.en;

  const gpsLabel = temple.hfLat && temple.hfLan
    ? `${temple.hfLat.toFixed(4)}° N, ${temple.hfLan.toFixed(4)}° E`
    : 'GPS Location Available';

  // Order options based on current view:
  // When viewing English: Tamil is at top (default suggested action), followed by Telugu, Hindi, and English at bottom.
  // When viewing non-English: English is at top (to easily switch back), followed by Tamil, Telugu, Hindi.
  const languageList: LanguageOption[] = displayedLang === 'en'
    ? [
        { code: 'ta', name: 'Tamil', native: 'தமிழ் (Tamil)' },
        { code: 'te', name: 'Telugu', native: 'తెలుగు (Telugu)' },
        { code: 'hi', name: 'Hindi', native: 'हिन्दी (Hindi)' },
        { code: 'en', name: 'English', native: 'English' },
      ]
    : [
        { code: 'en', name: 'English', native: 'English' },
        { code: 'ta', name: 'Tamil', native: 'தமிழ் (Tamil)' },
        { code: 'te', name: 'Telugu', native: 'తెలుగు (Telugu)' },
        { code: 'hi', name: 'Hindi', native: 'हिन्दी (Hindi)' },
      ];

  // Button label displayed on the combo box
  const comboLabel = displayedLang === 'en' ? 'தமிழ் (Tamil)' : 'English';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
      <div className="glass-panel w-full max-w-3xl max-h-[90vh] rounded-2xl overflow-hidden flex flex-col border border-slate-700 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="p-6 border-b border-slate-800 flex items-start justify-between bg-slate-900/80">
          <div className="flex-1 pr-4">
            <div className="flex items-center gap-2 flex-wrap mb-1">
              <span className="text-xs font-mono text-indigo-400 bg-indigo-950 border border-indigo-800 px-2 py-0.5 rounded">
                {t.temple} #{temple.id}
              </span>
              <span className="text-xs font-mono text-slate-400">
                {displayTemple.city}, {displayTemple.district}, {displayTemple.state}
              </span>
            </div>
            <h2 className="text-2xl font-extrabold text-white">{displayTemple.name}</h2>
            {displayTemple.historicalName && (
              <p className="text-xs text-slate-400 italic">
                {t.historicalName} {displayTemple.historicalName}
              </p>
            )}

            <div className="mt-3 flex items-center gap-3 flex-wrap">
              <button
                type="button"
                onClick={handleLocateOnMap}
                className="flex items-center gap-2 text-xs font-mono font-semibold text-indigo-300 bg-indigo-950/80 hover:bg-indigo-900 border border-indigo-700/60 px-3 py-1.5 rounded-xl transition cursor-pointer shadow-sm hover:shadow-indigo-500/20"
                title="Click to center and zoom map on this temple pin"
              >
                <Crosshair className="w-4 h-4 text-indigo-400 animate-pulse" />
                <span>📍 GPS: {gpsLabel} — ({t.locateMap})</span>
              </button>

              {/* Multi-Language Single-Select Combo Box */}
              <div className="relative inline-block" ref={dropdownRef}>
                <div className="flex items-center bg-slate-950/90 border border-slate-700/80 hover:border-indigo-500/60 rounded-xl shadow-sm transition">
                  {/* Primary Trigger / Quick Switch Button */}
                  <button
                    type="button"
                    onClick={() => {
                      if (displayedLang === 'en') {
                        handleLanguageChange('ta');
                      } else {
                        handleLanguageChange('en');
                      }
                    }}
                    disabled={isTranslating}
                    aria-label="Toggle language"
                    className="flex items-center gap-2 px-3 py-1.5 text-xs font-semibold text-white hover:text-indigo-300 transition cursor-pointer"
                    title={displayedLang === 'en' ? 'Click to translate to Tamil' : 'Click to show English'}
                  >
                    <Languages className="w-4 h-4 text-indigo-400 shrink-0" />
                    <span>{comboLabel}</span>
                  </button>

                  {/* Dropdown Chevron Toggle */}
                  <button
                    type="button"
                    onClick={() => setIsDropdownOpen((prev) => !prev)}
                    disabled={isTranslating}
                    aria-label="Open language selection menu"
                    className="px-2 py-1.5 text-slate-400 hover:text-white border-l border-slate-800 transition cursor-pointer"
                    title="Select other languages (Telugu, Hindi, English)"
                  >
                    {isTranslating ? (
                      <Loader2 className="w-3.5 h-3.5 text-indigo-400 animate-spin" />
                    ) : (
                      <ChevronDown className={`w-3.5 h-3.5 transition-transform duration-200 ${isDropdownOpen ? 'rotate-180' : ''}`} />
                    )}
                  </button>
                </div>

                {/* Dropdown Menu Popup */}
                {isDropdownOpen && (
                  <div
                    role="listbox"
                    aria-label="Available Languages"
                    className="absolute left-0 mt-1.5 w-52 bg-slate-900 border border-slate-700 rounded-xl shadow-2xl py-1.5 z-50 animate-in fade-in zoom-in-95 duration-150"
                  >
                    <div className="px-3 py-1 text-[10px] font-mono uppercase tracking-wider text-slate-400 border-b border-slate-800 mb-1">
                      {displayedLang === 'en' ? 'Choose Language:' : 'Switch Language:'}
                    </div>
                    {languageList.map((lang) => {
                      const isCurrent = displayedLang === lang.code;
                      return (
                        <button
                          key={lang.code}
                          type="button"
                          role="option"
                          aria-selected={isCurrent}
                          onClick={() => handleLanguageChange(lang.code)}
                          className={`w-full text-left px-3 py-2 text-xs flex items-center justify-between transition cursor-pointer ${
                            isCurrent
                              ? 'bg-indigo-950/60 text-indigo-300 font-semibold'
                              : 'text-slate-200 hover:bg-indigo-950/80 hover:text-indigo-200'
                          }`}
                        >
                          <span className="flex items-center gap-2">
                            <span>{lang.native}</span>
                            {isCurrent && (
                              <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-1.5 py-0.5 rounded">
                                Current
                              </span>
                            )}
                          </span>
                          {isCurrent && <Check className="w-3.5 h-3.5 text-indigo-400" />}
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white bg-slate-800/80 rounded-xl transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Translation Error Alert */}
        {translationError && (
          <div className="mx-6 mt-4 p-3 bg-amber-950/50 border border-amber-800/60 rounded-xl text-xs text-amber-300">
            {translationError}
          </div>
        )}

        {/* Loading Overlay when translating */}
        {isTranslating && (
          <div className="p-8 flex flex-col items-center justify-center space-y-3 bg-slate-900/40">
            <Loader2 className="w-8 h-8 text-indigo-400 animate-spin" />
            <p className="text-xs font-mono text-indigo-300">
              {t.translatingMsg}
            </p>
          </div>
        )}

        {/* Modal Body Content */}
        {!isTranslating && (
          <div className="p-6 overflow-y-auto space-y-6 text-sm text-slate-200">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 space-y-2">
                <h3 className="text-xs font-bold font-mono text-indigo-400 uppercase tracking-wider">
                  {t.deitiesWorship}
                </h3>
                <p><strong>{t.moolavar}</strong> {displayTemple.moolavar || 'N/A'}</p>
                <p><strong>{t.urchavar}</strong> {displayTemple.urchavar || 'N/A'}</p>
                <p><strong>{t.ammanThayar}</strong> {displayTemple.ammanThayar || 'N/A'}</p>
                <p><strong>{t.agamamPooja}</strong> {displayTemple.agamamPooja || 'N/A'}</p>
              </div>

              <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 space-y-2">
                <h3 className="text-xs font-bold font-mono text-purple-400 uppercase tracking-wider">
                  {t.sacredSymbols}
                </h3>
                <p><strong>{t.thalaVirutcham}</strong> {displayTemple.thalaVirutcham || 'N/A'}</p>
                <p><strong>{t.theertham}</strong> {displayTemple.theertham || 'N/A'}</p>
                <p><strong>{t.singers}</strong> {displayTemple.singers || 'N/A'}</p>
                <p><strong>{t.oldYear}</strong> {displayTemple.oldYear || 'N/A'}</p>
              </div>
            </div>

            {displayTemple.speciality && (
              <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800">
                <h3 className="text-xs font-bold font-mono text-emerald-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                  <Info className="w-4 h-4" />
                  {t.speciality}
                </h3>
                <p className="text-xs leading-relaxed text-slate-300">{displayTemple.speciality}</p>
              </div>
            )}

            {displayTemple.history && (
              <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800">
                <h3 className="text-xs font-bold font-mono text-amber-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                  <Calendar className="w-4 h-4" />
                  {t.history}
                </h3>
                <p className="text-xs leading-relaxed text-slate-300">{displayTemple.history}</p>
              </div>
            )}

            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
              <div>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Phone className="w-3.5 h-3.5 text-indigo-400" />
                  <span><strong>{t.phone}</strong> {displayTemple.phone || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Clock className="w-3.5 h-3.5 text-indigo-400" />
                  <span><strong>{t.openingTime}</strong> {displayTemple.openingTime || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300">
                  <MapPin className="w-3.5 h-3.5 text-indigo-400" />
                  <span><strong>{t.address}</strong> {displayTemple.address || displayTemple.location || 'N/A'}</span>
                </p>
              </div>
              <div>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Train className="w-3.5 h-3.5 text-purple-400" />
                  <span><strong>{t.railway}</strong> {displayTemple.nearByRailwayStation || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Plane className="w-3.5 h-3.5 text-purple-400" />
                  <span><strong>{t.airport}</strong> {displayTemple.nearByAirport || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300">
                  <ShieldAlert className="w-3.5 h-3.5 text-purple-400" />
                  <span><strong>{t.accommodation}</strong> {displayTemple.accommodation || 'N/A'}</span>
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 flex justify-between items-center bg-slate-900/80">
          <button
            type="button"
            onClick={handleLocateOnMap}
            className="flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300 font-semibold transition"
          >
            <Crosshair className="w-4 h-4" />
            <span>{t.locatePinFooter}</span>
          </button>

          <button
            onClick={onClose}
            className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs px-5 py-2 rounded-xl transition shadow-lg shadow-indigo-600/30"
          >
            {t.closeDetails}
          </button>
        </div>
      </div>
    </div>
  );
};
