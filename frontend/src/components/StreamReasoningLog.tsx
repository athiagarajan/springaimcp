import React, { useEffect, useRef } from 'react';
import { Terminal, Copy, Check, Activity } from 'lucide-react';

interface StreamReasoningLogProps {
  streamContent: string;
  isStreaming: boolean;
}

export const StreamReasoningLog: React.FC<StreamReasoningLogProps> = ({ streamContent, isStreaming }) => {
  const [copied, setCopied] = React.useState(false);
  const logEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [streamContent]);

  const handleCopy = () => {
    navigator.clipboard.writeText(streamContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="glass-panel rounded-2xl p-4 flex flex-col h-full border border-slate-800">
      <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <Terminal className="w-4 h-4 text-indigo-400" />
          <span className="text-xs font-mono font-semibold text-slate-200 uppercase tracking-wider">
            Spring AI Streaming Reasoning Log
          </span>
          {isStreaming && (
            <span className="flex items-center gap-1 text-[10px] font-mono text-emerald-400 bg-emerald-950/60 border border-emerald-500/30 px-2 py-0.5 rounded-full">
              <Activity className="w-3 h-3 animate-pulse" />
              LIVE SSE
            </span>
          )}
        </div>

        {streamContent && (
          <button
            onClick={handleCopy}
            className="flex items-center gap-1.5 text-xs text-slate-400 hover:text-slate-200 transition bg-slate-800/80 px-2.5 py-1 rounded-md border border-slate-700"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
            <span>{copied ? 'Copied' : 'Copy Log'}</span>
          </button>
        )}
      </div>

      <div className="flex-1 overflow-y-auto font-mono text-xs text-slate-300 leading-relaxed bg-slate-950/80 rounded-xl p-4 border border-slate-900 shadow-inner">
        {streamContent ? (
          <div className="whitespace-pre-wrap font-mono text-indigo-200/90">
            {streamContent}
            {isStreaming && <span className="inline-block w-2 h-4 bg-indigo-500 animate-pulse ml-1 align-middle"></span>}
          </div>
        ) : (
          <div className="h-full flex items-center justify-center text-slate-600 font-sans italic text-xs">
            Enter a prompt above to view real-time streaming LLM reasoning & MCP query logs...
          </div>
        )}
        <div ref={logEndRef} />
      </div>
    </div>
  );
};
