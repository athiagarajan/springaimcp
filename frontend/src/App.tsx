import React, { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { PromptBar } from './components/PromptBar';
import { StreamReasoningLog } from './components/StreamReasoningLog';
import { TempleMap } from './components/TempleMap';
import { TempleTable } from './components/TempleTable';
import { TempleDetailModal } from './components/TempleDetailModal';
import { Temple } from './types/temple';
import { fetchAllTemples, searchTemples, streamDynamicQuery } from './services/api';

export const App: React.FC = () => {
  const [mapTemples, setMapTemples] = useState<Temple[]>([]);
  const [tableTemples, setTableTemples] = useState<Temple[]>([]);
  const [selectedTemple, setSelectedTemple] = useState<Temple | null>(null);
  const [streamLog, setStreamLog] = useState<string>('');
  const [isStreaming, setIsStreaming] = useState<boolean>(false);
  const [isLoadingTemples, setIsLoadingTemples] = useState<boolean>(true);

  useEffect(() => {
    loadInitialTemples();
  }, []);

  const loadInitialTemples = async () => {
    try {
      setIsLoadingTemples(true);
      const data = await fetchAllTemples();
      // On start: populate map with initial temples, but leave bottom table empty
      setMapTemples(data);
      setTableTemples([]);
    } catch (err) {
      console.error('Failed to load initial temples:', err);
    } finally {
      setIsLoadingTemples(false);
    }
  };

  const handleSearch = async (prompt: string) => {
    // 1. Immediately clean up reasoning log, map pins, and temple record entries
    setStreamLog('');
    setMapTemples([]);
    setTableTemples([]);
    setIsStreaming(true);
    setIsLoadingTemples(true);

    try {
      // 2. Fetch matching temples (capped by prompt limit if specified)
      const filtered = await searchTemples(prompt);
      setTableTemples(filtered || []);
      setMapTemples(filtered || []);
    } catch (err) {
      console.error('Failed to search temples:', err);
    } finally {
      setIsLoadingTemples(false);
    }

    // 3. Stream AI reasoning log
    streamDynamicQuery(
      prompt,
      (chunk) => {
        setStreamLog((prev) => prev + chunk);
      },
      () => {
        setIsStreaming(false);
      },
      (err) => {
        console.error('SSE Error:', err);
        setIsStreaming(false);
      }
    );
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100 selection:bg-indigo-500 selection:text-white">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto p-6 space-y-6">
        <PromptBar onSearch={handleSearch} isLoading={isStreaming} />

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 h-[420px]">
          <StreamReasoningLog streamContent={streamLog} isStreaming={isStreaming} />
          <TempleMap
            temples={mapTemples}
            selectedTemple={selectedTemple}
            onSelectTemple={(t) => setSelectedTemple(t)}
          />
        </div>

        <TempleTable
          temples={tableTemples}
          onSelectTemple={(t) => setSelectedTemple(t)}
          isLoading={isLoadingTemples}
        />
      </main>

      <TempleDetailModal
        temple={selectedTemple}
        onClose={() => setSelectedTemple(null)}
        onSelectTemple={(t) => setSelectedTemple(t)}
      />
    </div>
  );
};

export default App;
