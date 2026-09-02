import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-cluster';
import L from 'leaflet';
import { Temple } from '../types/temple';
import { setGlobalDraggedTemple } from '../services/dragDropState';
import { MapPin, Navigation, Layers, Plus, Eye, GripVertical } from 'lucide-react';
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

// Component for individual draggable temple marker with smooth hover and off-hover auto-close
const DraggableTempleMarker: React.FC<{
  temple: Temple;
  lat: number;
  lng: number;
  onSelectTemple?: (temple: Temple) => void;
  onAddTemple?: (temple: Temple) => void;
}> = ({ temple, lat, lng, onSelectTemple, onAddTemple }) => {
  const map = useMap();
  const markerRef = useRef<L.Marker | null>(null);
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const cancelClose = () => {
    if (closeTimerRef.current) {
      clearTimeout(closeTimerRef.current);
      closeTimerRef.current = null;
    }
  };

  const scheduleClose = (delay = 250) => {
    cancelClose();
    closeTimerRef.current = setTimeout(() => {
      if (markerRef.current) {
        markerRef.current.closePopup();
      }
    }, delay);
  };

  useEffect(() => {
    const marker = markerRef.current;
    if (!marker) return;

    const el = marker.getElement();
    if (!el) return;

    // Enable native HTML5 drag on the marker element
    el.setAttribute('draggable', 'true');
    el.setAttribute('title', `${temple.name} (Drag pin to Temple Records table to add)`);
    el.style.cursor = 'grab';

    // Prevent Leaflet map from panning when pressing/dragging marker
    L.DomEvent.disableClickPropagation(el);

    const handleMouseDown = (e: MouseEvent) => {
      map.dragging.disable();
      e.stopPropagation();
    };

    const handleMouseUp = () => {
      map.dragging.enable();
    };

    const handleDragStart = (e: DragEvent) => {
      cancelClose();
      setGlobalDraggedTemple(temple);
      if (e.dataTransfer) {
        e.dataTransfer.setData('application/json', JSON.stringify(temple));
        e.dataTransfer.setData('text/plain', JSON.stringify(temple));
        e.dataTransfer.effectAllowed = 'copy';

        // Floating drag feedback badge
        const dragPreview = document.createElement('div');
        dragPreview.innerText = `🛕 ${temple.name}`;
        dragPreview.style.position = 'absolute';
        dragPreview.style.top = '-1000px';
        dragPreview.style.padding = '6px 12px';
        dragPreview.style.background = '#1e1b4b';
        dragPreview.style.color = '#ffffff';
        dragPreview.style.borderRadius = '8px';
        dragPreview.style.fontSize = '12px';
        dragPreview.style.fontWeight = 'bold';
        dragPreview.style.border = '1px solid #6366f1';
        dragPreview.style.boxShadow = '0 4px 12px rgba(0,0,0,0.5)';
        document.body.appendChild(dragPreview);
        e.dataTransfer.setDragImage(dragPreview, 10, 10);
        setTimeout(() => {
          if (document.body.contains(dragPreview)) {
            document.body.removeChild(dragPreview);
          }
        }, 0);
      }
    };

    const handleDragEnd = () => {
      map.dragging.enable();
      scheduleClose(100);
      setTimeout(() => setGlobalDraggedTemple(null), 300);
    };

    el.addEventListener('mousedown', handleMouseDown, true);
    el.addEventListener('mouseup', handleMouseUp);
    el.addEventListener('dragstart', handleDragStart);
    el.addEventListener('dragend', handleDragEnd);

    return () => {
      el.removeEventListener('mousedown', handleMouseDown, true);
      el.removeEventListener('mouseup', handleMouseUp);
      el.removeEventListener('dragstart', handleDragStart);
      el.removeEventListener('dragend', handleDragEnd);
      map.dragging.enable();
      cancelClose();
    };
  }, [temple, map]);

  return (
    <Marker
      ref={markerRef}
      position={[lat, lng]}
      icon={customIcon}
      eventHandlers={{
        mouseover: (e) => {
          cancelClose();
          e.target.openPopup();
        },
        mouseout: () => {
          // When mouse leaves the pin, give user a 300ms window to move into the popup
          scheduleClose(300);
        },
        click: () => {
          cancelClose();
          onSelectTemple && onSelectTemple(temple);
        }
      }}
    >
      {/* Details Popup shown on Hover with Auto-Close on off-hover */}
      <Popup className="custom-popup" autoPan={false}>
        <div
          className="p-2.5 font-sans min-w-[210px] max-w-[280px]"
          onMouseEnter={() => {
            // User entered popup, keep it open!
            cancelClose();
          }}
          onMouseLeave={() => {
            // Off hover popup: close immediately!
            scheduleClose(150);
          }}
        >
          <h3 className="font-bold text-slate-100 text-sm">{temple.name}</h3>
          <p className="text-xs text-slate-400 font-semibold">{temple.city || 'N/A'}, {temple.district || temple.state}</p>
          {temple.moolavar && (
            <p className="text-[11px] text-emerald-400 mt-1">
              <strong className="text-slate-400">Moolavar:</strong> {temple.moolavar}
            </p>
          )}

          <div className="mt-3 pt-2 border-t border-slate-800 flex items-center gap-2">
            <button
              type="button"
              onMouseDown={(e) => e.stopPropagation()}
              onClick={(e) => {
                e.stopPropagation();
                cancelClose();
                if (markerRef.current) markerRef.current.closePopup();
                onSelectTemple && onSelectTemple(temple);
              }}
              className="text-[11px] bg-indigo-600 hover:bg-indigo-500 text-white px-2.5 py-1 rounded-lg font-semibold transition cursor-pointer flex items-center gap-1"
            >
              <Eye className="w-3 h-3" />
              <span>Details</span>
            </button>

            {onAddTemple && (
              <button
                type="button"
                onMouseDown={(e) => e.stopPropagation()}
                onClick={(e) => {
                  e.stopPropagation();
                  onAddTemple(temple);
                  scheduleClose(400);
                }}
                className="text-[11px] bg-emerald-600 hover:bg-emerald-500 text-white px-2.5 py-1 rounded-lg font-semibold transition cursor-pointer flex items-center gap-1 shadow-sm"
                title="Add to Temple Records table"
              >
                <Plus className="w-3 h-3" />
                <span>Add to Table</span>
              </button>
            )}
          </div>

          <div
            draggable={true}
            onDragStart={(e) => {
              cancelClose();
              setGlobalDraggedTemple(temple);
              e.dataTransfer.setData('text/plain', JSON.stringify(temple));
              e.dataTransfer.effectAllowed = 'copy';
            }}
            onDragEnd={() => {
              scheduleClose(100);
            }}
            className="mt-2 text-center text-[10px] font-semibold text-slate-400 hover:text-white bg-slate-800/80 hover:bg-slate-700 py-1 px-2 rounded-lg border border-slate-700 cursor-grab active:cursor-grabbing flex items-center justify-center gap-1 transition"
            title="Press and drag down to Temple Records table"
          >
            <GripVertical className="w-3 h-3 text-slate-400" />
            <span>Drag to Records Table</span>
          </div>
        </div>
      </Popup>
    </Marker>
  );
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
      <DraggableTempleMarker
        key={temple.id}
        temple={temple}
        lat={lat}
        lng={lng}
        onSelectTemple={onSelectTemple}
        onAddTemple={onAddTemple}
      />
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
