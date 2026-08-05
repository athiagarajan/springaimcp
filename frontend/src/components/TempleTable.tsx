import React, { useState } from 'react';
import { Temple } from '../types/temple';
import { Search, MapPin, Eye, Calendar, Sparkles, ChevronDown, ChevronUp } from 'lucide-react';

interface TempleTableProps {
  temples: Temple[];
  onSelectTemple: (temple: Temple) => void;
  isLoading: boolean;
}

export const TempleTable: React.FC<TempleTableProps> = ({ temples, onSelectTemple, isLoading }) => {
  const [filterText, setFilterText] = useState('');
  const [isCollapsed, setIsCollapsed] = useState(false);

  const filteredTemples = temples.filter(t => 
    t.name.toLowerCase().includes(filterText.toLowerCase()) ||
    (t.city && t.city.toLowerCase().includes(filterText.toLowerCase())) ||
    (t.district && t.district.toLowerCase().includes(filterText.toLowerCase())) ||
    (t.moolavar && t.moolavar.toLowerCase().includes(filterText.toLowerCase()))
  );

  return (
    <div className="glass-panel rounded-2xl p-5 flex flex-col space-y-4 transition-all duration-300">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsCollapsed(!isCollapsed)}
            className="p-1 rounded-lg bg-slate-800/80 hover:bg-slate-700/80 text-slate-300 hover:text-white transition border border-slate-700/60"
            title={isCollapsed ? "Expand Temple Records" : "Collapse Temple Records"}
          >
            {isCollapsed ? <ChevronDown className="w-4 h-4 text-indigo-400" /> : <ChevronUp className="w-4 h-4 text-indigo-400" />}
          </button>

          <div>
            <h2 className="text-base font-bold text-slate-100 flex items-center gap-2">
              <span>Temple Records</span>
              <span className="text-xs font-mono font-normal text-indigo-400 bg-indigo-950/80 border border-indigo-800/60 px-2.5 py-0.5 rounded-full">
                {filteredTemples.length} records
              </span>
            </h2>
            <p className="text-xs text-slate-400">
              {isCollapsed ? "Click button on left to expand records view" : "Data retrieved from PostgreSQL templeinfo database"}
            </p>
          </div>
        </div>

        {!isCollapsed && temples.length > 0 && (
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={filterText}
              onChange={(e) => setFilterText(e.target.value)}
              placeholder="Quick filter results..."
              className="bg-slate-900 border border-slate-700/80 rounded-lg pl-9 pr-4 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
            />
          </div>
        )}
      </div>

      {!isCollapsed && (
        <>
          {isLoading ? (
            <div className="py-12 flex flex-col items-center justify-center text-slate-400 text-xs font-mono space-y-2">
              <Sparkles className="w-6 h-6 animate-spin text-indigo-400" />
              <span>Loading temple data...</span>
            </div>
          ) : filteredTemples.length === 0 ? (
            <div className="py-12 flex flex-col items-center justify-center text-slate-500 text-xs font-mono space-y-2 border border-dashed border-slate-800/80 rounded-xl bg-slate-950/40">
              <Search className="w-8 h-8 text-slate-600 mb-1" />
              <p className="text-slate-300 font-sans text-sm font-semibold">No Temple Records Displayed</p>
              <p className="text-slate-400 text-xs text-center max-w-md">
                Enter an AI prompt above (e.g. <span className="text-indigo-300 font-mono">"find murugan temple in thanjavur"</span>) to load and view matching temple details.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredTemples.map((temple) => (
                <div
                  key={temple.id}
                  onClick={() => onSelectTemple(temple)}
                  className="glass-card rounded-xl p-4 cursor-pointer flex flex-col justify-between space-y-3 group hover:border-indigo-500/50"
                >
                  <div>
                    <div className="flex items-start justify-between gap-2">
                      <h3 className="font-bold text-sm text-slate-100 group-hover:text-indigo-300 transition line-clamp-1">
                        {temple.name}
                      </h3>
                      <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded">
                        #{temple.id}
                      </span>
                    </div>

                    <div className="flex items-center gap-1 text-xs text-indigo-400 mt-1">
                      <MapPin className="w-3.5 h-3.5" />
                      <span>{temple.city || 'N/A'}, {temple.district || temple.state}</span>
                    </div>
                  </div>

                  <div className="space-y-1 text-xs text-slate-300 border-t border-slate-800/80 pt-2 font-mono">
                    {temple.moolavar && (
                      <div className="text-[11px] text-slate-400 truncate">
                        <span className="text-slate-500">Moolavar:</span> {temple.moolavar}
                      </div>
                    )}
                    {temple.festival && (
                      <div className="text-[11px] text-slate-400 truncate flex items-center gap-1">
                        <Calendar className="w-3 h-3 text-purple-400" />
                        <span className="truncate">{temple.festival}</span>
                      </div>
                    )}
                  </div>

                  <div className="flex items-center justify-between pt-1">
                    <span className="text-[10px] text-emerald-400 bg-emerald-950/60 border border-emerald-800/40 px-2 py-0.5 rounded">
                      {temple.hfLat ? '📍 GPS Map Linked' : 'No GPS Data'}
                    </span>
                    <button
                      type="button"
                      className="flex items-center gap-1 text-[11px] text-indigo-400 hover:text-indigo-200 font-semibold transition"
                    >
                      <span>Details</span>
                      <Eye className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
};
