import React, { useState, useEffect } from 'react';
import { Temple } from '../types/temple';
import { fetchTempleTranslation } from '../services/api';
import { X, MapPin, Calendar, Phone, Clock, Train, Plane, Info, ShieldAlert, Crosshair, Languages, Loader2 } from 'lucide-react';

interface TempleDetailModalProps {
  temple: Temple | null;
  onClose: () => void;
  onSelectTemple?: (temple: Temple) => void;
}

export const TempleDetailModal: React.FC<TempleDetailModalProps> = ({ temple, onClose, onSelectTemple }) => {
  const [language, setLanguage] = useState<'en' | 'ta'>('en');
  const [translatedTemple, setTranslatedTemple] = useState<Temple | null>(null);
  const [isTranslating, setIsTranslating] = useState<boolean>(false);
  const [translationError, setTranslationError] = useState<string | null>(null);

  // Reset state when a new temple is opened
  useEffect(() => {
    setLanguage('en');
    setTranslatedTemple(null);
    setIsTranslating(false);
    setTranslationError(null);
  }, [temple?.id]);

  if (!temple) return null;

  const handleLocateOnMap = () => {
    if (onSelectTemple) {
      onSelectTemple(temple);
    }
    onClose();
  };

  const handleToggleLanguage = async () => {
    if (language === 'en') {
      // Switch to Tamil
      setLanguage('ta');
      if (!translatedTemple && !isTranslating) {
        setIsTranslating(true);
        setTranslationError(null);
        try {
          const result = await fetchTempleTranslation(temple.id, 'ta');
          setTranslatedTemple(result);
        } catch (err: any) {
          console.error('Translation error:', err);
          setTranslationError('Unable to translate details right now. Showing English version.');
        } finally {
          setIsTranslating(false);
        }
      }
    } else {
      // Switch back to English
      setLanguage('en');
    }
  };

  const displayTemple = (language === 'ta' && translatedTemple) ? translatedTemple : temple;

  const gpsLabel = temple.hfLat && temple.hfLan
    ? `${temple.hfLat.toFixed(4)}° N, ${temple.hfLan.toFixed(4)}° E`
    : 'GPS Location Available';

  const isTa = language === 'ta';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
      <div className="glass-panel w-full max-w-3xl max-h-[90vh] rounded-2xl overflow-hidden flex flex-col border border-slate-700 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="p-6 border-b border-slate-800 flex items-start justify-between bg-slate-900/80">
          <div className="flex-1 pr-4">
            <div className="flex items-center gap-2 flex-wrap mb-1">
              <span className="text-xs font-mono text-indigo-400 bg-indigo-950 border border-indigo-800 px-2 py-0.5 rounded">
                {isTa ? 'திருக்கோயில்' : 'Temple'} #{temple.id}
              </span>
              <span className="text-xs font-mono text-slate-400">
                {displayTemple.city}, {displayTemple.district}, {displayTemple.state}
              </span>
            </div>
            <h2 className="text-2xl font-extrabold text-white">{displayTemple.name}</h2>
            {displayTemple.historicalName && (
              <p className="text-xs text-slate-400 italic">
                {isTa ? 'வரலாற்றுப் பெயர்: ' : 'Historical Name: '}{displayTemple.historicalName}
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
                <span>
                  📍 GPS: {gpsLabel} — ({isTa ? 'வரைபடத்தில் பார்க்க' : 'Click to Locate Pin on Map'})
                </span>
              </button>

              {/* Language Toggle Button */}
              <button
                type="button"
                onClick={handleToggleLanguage}
                disabled={isTranslating}
                className={`flex items-center gap-2 text-xs font-semibold px-3 py-1.5 rounded-xl border transition cursor-pointer shadow-sm ${
                  language === 'en'
                    ? 'bg-emerald-950/80 hover:bg-emerald-900 text-emerald-300 border-emerald-700/60 hover:shadow-emerald-500/20'
                    : 'bg-amber-950/80 hover:bg-amber-900 text-amber-300 border-amber-700/60 hover:shadow-amber-500/20'
                }`}
                title={language === 'en' ? 'Click to translate temple details to Tamil' : 'Click to show English details'}
              >
                {isTranslating ? (
                  <Loader2 className="w-4 h-4 text-emerald-400 animate-spin" />
                ) : (
                  <Languages className="w-4 h-4" />
                )}
                <span>
                  {isTranslating
                    ? (isTa ? 'தமிழில் மொழிபெயர்க்கப்படுகிறது...' : 'Translating to Tamil...')
                    : language === 'en'
                    ? 'தமிழ்'
                    : 'English'}
                </span>
              </button>
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
            <Loader2 className="w-8 h-8 text-emerald-400 animate-spin" />
            <p className="text-xs font-mono text-emerald-300">
              {isTa ? 'ஜெமினி AI மூலம் கோயிலின் அனைத்து விவரங்களும் தமிழில் மொழிபெயர்க்கப்படுகிறது...' : 'Translating all temple details to Tamil via Gemini AI...'}
            </p>
          </div>
        )}

        {/* Modal Body Content */}
        {!isTranslating && (
          <div className="p-6 overflow-y-auto space-y-6 text-sm text-slate-200">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 space-y-2">
                <h3 className="text-xs font-bold font-mono text-indigo-400 uppercase tracking-wider">
                  {isTa ? 'மூலவர் & வழிபாடுகள்' : 'Deities & Worship'}
                </h3>
                <p><strong>{isTa ? 'மூலவர்:' : 'Moolavar:'}</strong> {displayTemple.moolavar || 'N/A'}</p>
                <p><strong>{isTa ? 'உற்சவர்:' : 'Urchavar:'}</strong> {displayTemple.urchavar || 'N/A'}</p>
                <p><strong>{isTa ? 'அம்மன் / தாயார்:' : 'Amman / Thayar:'}</strong> {displayTemple.ammanThayar || 'N/A'}</p>
                <p><strong>{isTa ? 'ஆகமம் / பூஜை:' : 'Agamam / Pooja:'}</strong> {displayTemple.agamamPooja || 'N/A'}</p>
              </div>

              <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 space-y-2">
                <h3 className="text-xs font-bold font-mono text-purple-400 uppercase tracking-wider">
                  {isTa ? 'தல விருட்சம் & புனித குறியீடுகள்' : 'Sacred Symbols'}
                </h3>
                <p><strong>{isTa ? 'தல விருட்சம்:' : 'Thala Virutcham:'}</strong> {displayTemple.thalaVirutcham || 'N/A'}</p>
                <p><strong>{isTa ? 'தீர்த்தம்:' : 'Theertham:'}</strong> {displayTemple.theertham || 'N/A'}</p>
                <p><strong>{isTa ? 'பாடியவர்கள்:' : 'Singers:'}</strong> {displayTemple.singers || 'N/A'}</p>
                <p><strong>{isTa ? 'பழமை / அமைக்கப்பட்ட ஆண்டு:' : 'Old / Construction Year:'}</strong> {displayTemple.oldYear || 'N/A'}</p>
              </div>
            </div>

            {displayTemple.speciality && (
              <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800">
                <h3 className="text-xs font-bold font-mono text-emerald-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                  <Info className="w-4 h-4" />
                  {isTa ? 'கோயில் சிறப்புகள் & முக்கியத்துவம்' : 'Speciality & Importance'}
                </h3>
                <p className="text-xs leading-relaxed text-slate-300">{displayTemple.speciality}</p>
              </div>
            )}

            {displayTemple.history && (
              <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800">
                <h3 className="text-xs font-bold font-mono text-amber-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                  <Calendar className="w-4 h-4" />
                  {isTa ? 'கோயில் வரலாறு' : 'Temple History'}
                </h3>
                <p className="text-xs leading-relaxed text-slate-300">{displayTemple.history}</p>
              </div>
            )}

            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
              <div>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Phone className="w-3.5 h-3.5 text-indigo-400" />
                  <span><strong>{isTa ? 'தொலைபேசி:' : 'Phone:'}</strong> {displayTemple.phone || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Clock className="w-3.5 h-3.5 text-indigo-400" />
                  <span><strong>{isTa ? 'நடை திறக்கும் நேரம்:' : 'Opening Timings:'}</strong> {displayTemple.openingTime || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300">
                  <MapPin className="w-3.5 h-3.5 text-indigo-400" />
                  <span><strong>{isTa ? 'முகவரி:' : 'Address:'}</strong> {displayTemple.address || displayTemple.location || 'N/A'}</span>
                </p>
              </div>
              <div>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Train className="w-3.5 h-3.5 text-purple-400" />
                  <span><strong>{isTa ? 'அருகிலுள்ள ரயில் நிலையம்:' : 'Nearest Railway:'}</strong> {displayTemple.nearByRailwayStation || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300 mb-1">
                  <Plane className="w-3.5 h-3.5 text-purple-400" />
                  <span><strong>{isTa ? 'அருகிலுள்ள விமான நிலையம்:' : 'Nearest Airport:'}</strong> {displayTemple.nearByAirport || 'N/A'}</span>
                </p>
                <p className="flex items-center gap-2 text-slate-300">
                  <ShieldAlert className="w-3.5 h-3.5 text-purple-400" />
                  <span><strong>{isTa ? 'தங்குமிடம்:' : 'Accommodation:'}</strong> {displayTemple.accommodation || 'N/A'}</span>
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
            <span>{isTa ? 'வரைபடத்தில் இருப்பிடத்தைக் காட்டு' : 'Locate Pin on Map'}</span>
          </button>

          <button
            onClick={onClose}
            className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs px-5 py-2 rounded-xl transition shadow-lg shadow-indigo-600/30"
          >
            {isTa ? 'விவரங்களை மூடுக' : 'Close Details'}
          </button>
        </div>
      </div>
    </div>
  );
};
