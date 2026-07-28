import React, { useState } from 'react';
import { Send, Sparkles, X, RefreshCw } from 'lucide-react';

interface PromptBarProps {
  onSearch: (prompt: string) => void;
  isLoading: boolean;
}

const PRESET_PROMPTS = [
  "Find temples in Dindigul or Palani dedicated to Lord Shiva or Idumban",
  "Temples with nearest railway station in Chennai or Kurnool",
  "Ancient temples in Tamil Nadu with unique sthala virutcham and festivals",
  "Temples located near airports with accommodation options"
];

export const PromptBar: React.FC<PromptBarProps> = ({ onSearch, isLoading }) => {
  const [prompt, setPrompt] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (prompt.trim() && !isLoading) {
      onSearch(prompt.trim());
    }
  };

  const handlePresetClick = (preset: string) => {
    setPrompt(preset);
    onSearch(preset);
  };

  return (
    <div className="glass-panel p-5 rounded-2xl shadow-2xl space-y-4">
      <form onSubmit={handleSubmit} className="relative flex items-center">
        <div className="absolute left-4 text-indigo-400">
          <Sparkles className="w-5 h-5 animate-pulse" />
        </div>
        <input
          type="text"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Ask anything about the temples database in natural language..."
          className="w-full bg-slate-900/90 border border-slate-700/80 rounded-xl pl-12 pr-28 py-3.5 text-sm text-slate-100 placeholder-slate-400 focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 transition-all"
          disabled={isLoading}
        />
        <div className="absolute right-2 flex items-center gap-2">
          {prompt && (
            <button
              type="button"
              onClick={() => setPrompt('')}
              className="p-1.5 text-slate-400 hover:text-slate-200 transition"
              disabled={isLoading}
            >
              <X className="w-4 h-4" />
            </button>
          )}
          <button
            type="submit"
            disabled={!prompt.trim() || isLoading}
            className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 disabled:opacity-50 text-white text-xs font-semibold px-4 py-2.5 rounded-lg shadow-md shadow-indigo-600/30 transition-all"
          >
            {isLoading ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Streaming...</span>
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                <span>Ask AI</span>
              </>
            )}
          </button>
        </div>
      </form>

      {/* Preset Suggestions */}
      <div className="flex items-center gap-2 flex-wrap text-xs">
        <span className="text-slate-400 font-mono text-[11px]">Suggestions:</span>
        {PRESET_PROMPTS.map((preset, index) => (
          <button
            key={index}
            type="button"
            onClick={() => handlePresetClick(preset)}
            className="bg-slate-800/60 hover:bg-indigo-900/40 text-slate-300 hover:text-indigo-200 border border-slate-700/50 hover:border-indigo-500/40 px-3 py-1 rounded-full transition text-[11px]"
            disabled={isLoading}
          >
            {preset}
          </button>
        ))}
      </div>
    </div>
  );
};
