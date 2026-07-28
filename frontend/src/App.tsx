import React, { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { PromptBar } from './components/PromptBar';
import { StreamReasoningLog } from './components/StreamReasoningLog';
import { TempleMap } from './components/TempleMap';
import { TempleTable } from './components/TempleTable';
import { TempleDetailModal } from './components/TempleDetailModal';
import { Temple } from './types/temple';
import { fetchAllTemples, streamDynamicQuery } from './services/api';

export const App: React.FC = () => {
  const [temples, setTemples] = useState<Temple[]>([]);
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
      setTemples(data);
    } catch (err) {
      console.error('Failed to load initial temples:', err);
    } finally {
      setIsLoadingTemples(false);
    }
  };

  const handleSearch = (prompt: string) => {
    setStreamLog('');
    setIsStreaming(true);

    streamDynamicQuery(
      prompt,
      (chunk) => {
        setStreamLog((prev) => prev + chunk);
      },
      () => {
        setIsStreaming(false);
        // Refresh temples after streaming query
        loadInitialTemples();
      },
      (err) => {
        console.error('SSE Error:', err);
        setStreamLog((prev) => prev + '\n[Stream Error]: Connection closed or local LLM server unavailable.');
        setIsStreaming(false);
      }
    );
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100 selection:bg-indigo-500 selection:text-white">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto p-6 space-y-6">
        {/* Top Prompt Section */}
        <PromptBar onSearch={handleSearch} isLoading={isStreaming} />

        {/* Middle Split-View Section: Stream Log & Map */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 h-[420px]">
          <StreamReasoningLog streamContent={streamLog} isStreaming={isStreaming} />
          <TempleMap temples={temples} onSelectTemple={(t) => setSelectedTemple(t)} />
        </div>

        {/* Bottom Section: Temple Records Table & Grid */}
        <TempleTable
          temples={temples}
          onSelectTemple={(t) => setSelectedTemple(t)}
          isLoading={isLoadingTemples}
        />
      </main>

      {/* Modal View */}
      <TempleDetailModal temple={selectedTemple} onClose={() => setSelectedTemple(null)} />
    </div>
  );
};

export default App;
