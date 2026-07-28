import React from 'react';
import { Temple } from '../types/temple';
import { X, MapPin, Calendar, Phone, Clock, Train, Plane, Info, ShieldAlert } from 'lucide-react';

interface TempleDetailModalProps {
  temple: Temple | null;
  onClose: () => void;
}

export const TempleDetailModal: React.FC<TempleDetailModalProps> = ({ temple, onClose }) => {
  if (!temple) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
      <div className="glass-panel w-full max-w-3xl max-h-[90vh] rounded-2xl overflow-hidden flex flex-col border border-slate-700 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
        {/* Modal Header */}
        <div className="p-6 border-b border-slate-800 flex items-start justify-between bg-slate-900/80">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono text-indigo-400 bg-indigo-950 border border-indigo-800 px-2 py-0.5 rounded">
                Temple #{temple.id}
              </span>
              <span className="text-xs font-mono text-slate-400">
                {temple.city}, {temple.district}, {temple.state}
              </span>
            </div>
            <h2 className="text-2xl font-extrabold text-white mt-1">{temple.name}</h2>
            {temple.historicalName && (
              <p className="text-xs text-slate-400 italic">Historical Name: {temple.historicalName}</p>
            )}
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white bg-slate-800/80 rounded-xl transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-6 text-sm text-slate-200">
          {/* Key Attributes Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 space-y-2">
              <h3 className="text-xs font-bold font-mono text-indigo-400 uppercase tracking-wider">Deities & Worship</h3>
              <p><strong>Moolavar:</strong> {temple.moolavar || 'N/A'}</p>
              <p><strong>Urchavar:</strong> {temple.urchavar || 'N/A'}</p>
              <p><strong>Amman / Thayar:</strong> {temple.ammanThayar || 'N/A'}</p>
              <p><strong>Agamam / Pooja:</strong> {temple.agamamPooja || 'N/A'}</p>
            </div>

            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 space-y-2">
              <h3 className="text-xs font-bold font-mono text-purple-400 uppercase tracking-wider">Sacred Symbols</h3>
              <p><strong>Thala Virutcham:</strong> {temple.thalaVirutcham || 'N/A'}</p>
              <p><strong>Theertham:</strong> {temple.theertham || 'N/A'}</p>
              <p><strong>Singers:</strong> {temple.singers || 'N/A'}</p>
              <p><strong>Old / Construction Year:</strong> {temple.oldYear || 'N/A'}</p>
            </div>
          </div>

          {/* Speciality & Greatness */}
          {temple.speciality && (
            <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800">
              <h3 className="text-xs font-bold font-mono text-emerald-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <Info className="w-4 h-4" />
                Speciality & Importance
              </h3>
              <p className="text-xs leading-relaxed text-slate-300">{temple.speciality}</p>
            </div>
          )}

          {/* History */}
          {temple.history && (
            <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800">
              <h3 className="text-xs font-bold font-mono text-amber-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <Calendar className="w-4 h-4" />
                Temple History
              </h3>
              <p className="text-xs leading-relaxed text-slate-300">{temple.history}</p>
            </div>
          )}

          {/* Logistics & Amenities */}
          <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div>
              <p className="flex items-center gap-2 text-slate-300 mb-1">
                <Phone className="w-3.5 h-3.5 text-indigo-400" />
                <span><strong>Phone:</strong> {temple.phone || 'N/A'}</span>
              </p>
              <p className="flex items-center gap-2 text-slate-300 mb-1">
                <Clock className="w-3.5 h-3.5 text-indigo-400" />
                <span><strong>Opening Timings:</strong> {temple.openingTime || 'N/A'}</span>
              </p>
              <p className="flex items-center gap-2 text-slate-300">
                <MapPin className="w-3.5 h-3.5 text-indigo-400" />
                <span><strong>Address:</strong> {temple.address || temple.location || 'N/A'}</span>
              </p>
            </div>
            <div>
              <p className="flex items-center gap-2 text-slate-300 mb-1">
                <Train className="w-3.5 h-3.5 text-purple-400" />
                <span><strong>Nearest Railway:</strong> {temple.nearByRailwayStation || 'N/A'}</span>
              </p>
              <p className="flex items-center gap-2 text-slate-300 mb-1">
                <Plane className="w-3.5 h-3.5 text-purple-400" />
                <span><strong>Nearest Airport:</strong> {temple.nearByAirport || 'N/A'}</span>
              </p>
              <p className="flex items-center gap-2 text-slate-300">
                <ShieldAlert className="w-3.5 h-3.5 text-purple-400" />
                <span><strong>Accommodation:</strong> {temple.accommodation || 'N/A'}</span>
              </p>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="p-4 border-t border-slate-800 flex justify-end bg-slate-900/80">
          <button
            onClick={onClose}
            className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs px-5 py-2 rounded-xl transition shadow-lg shadow-indigo-600/30"
          >
            Close Details
          </button>
        </div>
      </div>
    </div>
  );
};
