import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Tooltip, useMap } from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-cluster';
import L from 'leaflet';
import { Temple } from '../types/temple';
import { MapPin, Navigation, Layers, Plus } from 'lucide-react';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';

interface TempleMapProps {
  temples: Temple[];
  selectedTemple?: Temple | null;
  onSelectTemple?: (temple: Temple) => void;
  onAddTemple?: (temple: Temple) => void;
}

const customIcon = new L.Icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const DISTRICT_COORDS: Record<string, [number, number]> = {
  thanjavur: [10.7870, 79.1378],
  tirunelveli: [8.7139, 77.7567],
  salem: [11.6643, 78.1460],
  madurai: [9.9252, 78.1198],
  dindigul: [10.3673, 77.9803],
  kanchipuram: [12.8342, 79.7036],
  nagapattinam: [10.7672, 79.8449],
  tiruvarur: [10.7705, 79.6354],
};

const MapRecenter: React.FC<{
  selectedTemple?: Temple | null;
  mapItems: Array<{ temple: Temple; lat: number; lng: number }>;
}> = ({ selectedTemple, mapItems }) => {
  const map = useMap();

  useEffect(() => {
    if (selectedTemple) {
      const match = mapItems.find(item => item.temple.id === selectedTemple.id);
      if (match) {
        map.flyTo([match.lat, match.lng], 14, { duration: 1.2 });
      }
    }
  }, [selectedTemple, mapItems, map]);

  return null;
};

export const TempleMap: React.FC<TempleMapProps> = ({ temples, selectedTemple, onSelectTemple, onAddTemple }) => {
  const [enableClustering, setEnableClustering] = useState<boolean>(true);

  // 1. Initial coordinates assignment for each temple
  const rawMapItems = temples.map((temple, idx) => {
    let lat = temple.hfLat;
    let lng = temple.hfLan;

    if (lat == null || lng == null) {
      const distKey = (temple.district || temple.city || '').toLowerCase();
      const matchKey = Object.keys(DISTRICT_COORDS).find(k => distKey.includes(k));
      const base = matchKey ? DISTRICT_COORDS[matchKey] : [10.7905, 78.7047];

      // Spiral arrangement for temples with missing coordinates
      const angle = idx * (2 * Math.PI / Math.min(temples.length, 8));
      const radius = 0.015 + (Math.floor(idx / 8) * 0.01);
      lat = base[0] + radius * Math.cos(angle);
      lng = base[1] + radius * Math.sin(angle);
    }

    return { temple, lat: lat!, lng: lng! };
  });

  // 2. Disambiguate overlapping or duplicate coordinates so EVERY pin is distinctly visible and clickable
  const coordinateCounts = new Map<string, number>();
  const mapItems = rawMapItems.map((item) => {
    const key = `${item.lat.toFixed(4)},${item.lng.toFixed(4)}`;
    const count = coordinateCounts.get(key) || 0;
    coordinateCounts.set(key, count + 1);

    if (count > 0) {
      const angle = count * (2 * Math.PI / 6);
      const radius = 0.006 * Math.ceil(count / 6);
      return {
        ...item,
        lat: item.lat + radius * Math.cos(angle),
        lng: item.lng + radius * Math.sin(angle),
      };
    }
    return item;
  });
  
  const defaultCenter: [number, number] = mapItems.length > 0 
    ? [mapItems[0].lat, mapItems[0].lng] 
    : [10.7905, 78.7047];

  const renderMarkers = () =>
    mapItems.map(({ temple, lat, lng }) => (
      <Marker 
        key={temple.id} 
        position={[lat, lng]} 
        icon={customIcon}
        eventHandlers={{
          add: (e) => {
            const el = e.target.getElement();
            if (el) {
              // Enable HTML5 drag from map pin to temple records table
              el.setAttribute('draggable', 'true');
              el.setAttribute('title', `${temple.name} (Drag pin to Temple Records table to add)`);
              el.style.cursor = 'grab';

              el.ondragstart = (dragEvt: DragEvent) => {
                if (dragEvt.dataTransfer) {
                  dragEvt.dataTransfer.setData('application/json', JSON.stringify(temple));
                  dragEvt.dataTransfer.setData('text/plain', String(temple.id));
                  dragEvt.dataTransfer.effectAllowed = 'copy';
                }
              };
            }
          },
          click: () => onSelectTemple && onSelectTemple(temple)
        }}
      >
        {/* Minimal Details Tooltip shown on Hover */}
        <Tooltip direction="top" offset={[0, -28]} opacity={1}>
          <div className="p-2.5 min-w-[200px] max-w-[280px] font-sans bg-slate-900/95 text-slate-100 rounded-xl shadow-xl border border-slate-700 pointer-events-auto">
            <h4 className="font-bold text-xs text-indigo-300 line-clamp-1">{temple.name}</h4>
            <p className="text-[11px] text-slate-400 font-medium">{temple.city || 'N/A'}, {temple.district || temple.state}</p>
            {temple.moolavar && (
              <p className="text-[11px] text-emerald-400 mt-1 line-clamp-1">
                <strong className="text-slate-400">Moolavar:</strong> {temple.moolavar}
              </p>
            )}
            <div className="mt-2 pt-1.5 border-t border-slate-800 flex items-center justify-between text-[10px]">
              <span className="text-slate-400 italic">Drag to Records Table</span>
              {onAddTemple && (
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onAddTemple(temple);
                  }}
                  className="bg-indigo-600 hover:bg-indigo-500 text-white px-2 py-0.5 rounded text-[10px] font-semibold cursor-pointer transition shadow-sm flex items-center gap-1"
                  title="Add this temple to records list"
                >
                  <Plus className="w-2.5 h-2.5" />
                  <span>Add to Table</span>
                </button>
              )}
            </div>
          </div>
        </Tooltip>

        {/* Full Details Popup shown on Click */}
        <Popup className="custom-popup">
          <div className="p-1 font-sans">
            <h3 className="font-bold text-slate-900 text-sm">{temple.name}</h3>
            <p className="text-xs text-slate-600 font-semibold">{temple.city || 'N/A'}, {temple.district || temple.state}</p>
            {temple.moolavar && <p className="text-[11px] text-slate-500 mt-1"><strong>Moolavar:</strong> {temple.moolavar}</p>}
            <div className="mt-2 flex items-center gap-2">
              <button
                type="button"
                onClick={() => onSelectTemple && onSelectTemple(temple)}
                className="text-[11px] bg-indigo-600 hover:bg-indigo-700 text-white px-2.5 py-1 rounded font-semibold transition cursor-pointer"
              >
                View Details
              </button>
              {onAddTemple && (
                <button
                  type="button"
                  onClick={() => onAddTemple(temple)}
                  className="text-[11px] bg-slate-700 hover:bg-slate-600 text-white px-2 py-1 rounded font-semibold transition cursor-pointer flex items-center gap-1"
                >
                  <Plus className="w-3 h-3" />
                  <span>Add to Table</span>
                </button>
              )}
            </div>
          </div>
        </Popup>
      </Marker>
    ));

  return (
    <div className="glass-panel rounded-2xl p-4 flex flex-col h-full border border-slate-800 relative">
      <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <MapPin className="w-4 h-4 text-purple-400" />
          <span className="text-xs font-mono font-semibold text-slate-200 uppercase tracking-wider">
            Interactive Temple Locations Map
          </span>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setEnableClustering(!enableClustering)}
            className={`text-xs font-mono px-2.5 py-1 rounded-full border transition flex items-center gap-1.5 cursor-pointer ${
              enableClustering
                ? 'text-purple-300 bg-purple-950/80 border-purple-700/60 hover:bg-purple-900/80'
                : 'text-slate-400 bg-slate-800/80 border-slate-700 hover:text-slate-200'
            }`}
            title="Toggle Cluster / Declustering of map pins"
          >
            <Layers className="w-3.5 h-3.5 text-purple-400" />
            <span>{enableClustering ? 'Cluster ON' : 'Cluster OFF'}</span>
          </button>

          <span className="text-xs font-mono text-purple-300 bg-purple-950/60 border border-purple-800/40 px-2.5 py-0.5 rounded-full">
            {mapItems.length} Map Pins
          </span>
        </div>
      </div>

      <div className="flex-1 rounded-xl overflow-hidden border border-slate-800 z-10 min-h-[300px]">
        {mapItems.length > 0 ? (
          <MapContainer center={defaultCenter} zoom={7} scrollWheelZoom={true} className="w-full h-full min-h-[320px]">
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <MapRecenter selectedTemple={selectedTemple} mapItems={mapItems} />

            {enableClustering ? (
              <MarkerClusterGroup
                chunkedLoading
                maxClusterRadius={50}
                spiderfyOnMaxZoom={true}
                showCoverageOnHover={false}
              >
                {renderMarkers()}
              </MarkerClusterGroup>
            ) : (
              renderMarkers()
            )}
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
