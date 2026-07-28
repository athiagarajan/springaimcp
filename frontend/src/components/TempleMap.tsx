import React from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { Temple } from '../types/temple';
import { MapPin, Navigation } from 'lucide-react';

interface TempleMapProps {
  temples: Temple[];
  onSelectTemple?: (temple: Temple) => void;
}

// Custom Leaflet Pin Icon
const customIcon = new L.Icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

export const TempleMap: React.FC<TempleMapProps> = ({ temples, onSelectTemple }) => {
  // Filter temples with valid lat/long
  const validTemples = temples.filter(t => t.hfLat != null && t.hfLan != null);
  
  // Default map center (Tamil Nadu / South India coordinates)
  const defaultCenter: [number, number] = validTemples.length > 0 
    ? [validTemples[0].hfLat!, validTemples[0].hfLan!] 
    : [10.7905, 78.7047];

  return (
    <div className="glass-panel rounded-2xl p-4 flex flex-col h-full border border-slate-800 relative">
      <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <MapPin className="w-4 h-4 text-purple-400" />
          <span className="text-xs font-mono font-semibold text-slate-200 uppercase tracking-wider">
            Interactive Temple Locations Map
          </span>
        </div>
        <span className="text-xs font-mono text-purple-300 bg-purple-950/60 border border-purple-800/40 px-2.5 py-0.5 rounded-full">
          {validTemples.length} Map Pins
        </span>
      </div>

      <div className="flex-1 rounded-xl overflow-hidden border border-slate-800 z-10 min-h-[300px]">
        {validTemples.length > 0 ? (
          <MapContainer center={defaultCenter} zoom={7} scrollWheelZoom={true} className="w-full h-full min-h-[320px]">
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {validTemples.map(temple => (
              <Marker 
                key={temple.id} 
                position={[temple.hfLat!, temple.hfLan!]} 
                icon={customIcon}
                eventHandlers={{
                  click: () => onSelectTemple && onSelectTemple(temple)
                }}
              >
                <Popup className="custom-popup">
                  <div className="p-1 font-sans">
                    <h3 className="font-bold text-slate-900 text-sm">{temple.name}</h3>
                    <p className="text-xs text-slate-600 font-semibold">{temple.city}, {temple.district}, {temple.state}</p>
                    {temple.moolavar && <p className="text-[11px] text-slate-500 mt-1"><strong>Moolavar:</strong> {temple.moolavar}</p>}
                    <button
                      onClick={() => onSelectTemple && onSelectTemple(temple)}
                      className="mt-2 text-[11px] bg-indigo-600 hover:bg-indigo-700 text-white px-2 py-1 rounded font-semibold transition"
                    >
                      View Details
                    </button>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        ) : (
          <div className="h-full flex flex-col items-center justify-center text-slate-500 text-xs font-mono p-6 text-center">
            <Navigation className="w-8 h-8 mb-2 text-slate-600 animate-bounce" />
            <span>No geographic coordinates to render on map.</span>
          </div>
        )}
      </div>
    </div>
  );
};
