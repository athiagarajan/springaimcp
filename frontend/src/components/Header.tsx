import React from 'react';
import { Cpu, Database, Server, ExternalLink, ShieldCheck } from 'lucide-react';

export const Header: React.FC = () => {
  return (
    <header className="glass-panel sticky top-0 z-40 border-b border-slate-800 px-6 py-4 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-purple-500 flex items-center justify-center shadow-lg shadow-indigo-500/25">
          <Cpu className="h-5 w-5 text-white" />
        </div>
        <div>
          <h1 className="text-xl font-bold bg-gradient-to-r from-white via-slate-200 to-indigo-300 bg-clip-text text-transparent">
            springaimcp Explorer
          </h1>
          <p className="text-xs text-slate-400 font-mono flex items-center gap-2">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            Spring Boot 4.0 • Java 25 • Jackson 3 • Spring AI 2.0 MCP • PostgreSQL (templeinfo)
          </p>
        </div>
      </div>

      <div className="flex items-center gap-4 text-xs font-mono text-slate-300">
        <div className="flex items-center gap-1.5 bg-slate-900/80 px-3 py-1.5 rounded-lg border border-slate-800">
          <Database className="w-3.5 h-3.5 text-indigo-400" />
          <span>templeinfo (96 temples)</span>
        </div>
        <div className="flex items-center gap-1 text-emerald-400 bg-emerald-950/60 border border-emerald-800/60 px-2.5 py-1 rounded-lg">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Basic Auth Secured</span>
        </div>
        <a
          href="http://localhost:8080/swagger-ui.html"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1.5 bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 border border-indigo-500/30 px-3 py-1.5 rounded-lg transition"
        >
          <Server className="w-3.5 h-3.5" />
          <span>Protected Swagger UI</span>
          <ExternalLink className="w-3 h-3 ml-0.5" />
        </a>
      </div>
    </header>
  );
};
