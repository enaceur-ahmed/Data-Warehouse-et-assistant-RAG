/* Layout : sidebar de navigation + barre supérieure + contenu */

import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { apiInfo } from '../api/client';
import {
  GridIcon,
  BriefcaseIcon,
  BuildingIcon,
  UsersIcon,
  ChatIcon,
  MenuIcon,
} from './icons';

const NAV_ITEMS = [
  { to: '/page1', label: 'Départements', icon: GridIcon },
  { to: '/page2', label: 'Postes', icon: BriefcaseIcon },
  { to: '/page3', label: 'Sociétés', icon: BuildingIcon },
  { to: '/page4', label: 'Managers', icon: UsersIcon },
];

const ASSISTANT_NAV_ITEM = { to: '/assistant', label: 'Assistant', icon: ChatIcon };

const PAGE_META = {
  '/page1': {
    title: 'Analyse par Département',
    subtitle: 'Effectifs, salaires moyens et répartition par genre',
  },
  '/page2': {
    title: 'Analyse par Poste',
    subtitle: 'Répartition des employés et salaires par titre de poste',
  },
  '/page3': {
    title: 'Analyse par Société',
    subtitle: 'Effectifs, masse salariale et salaire moyen par société',
  },
  '/page4': {
    title: 'Hiérarchie & Managers',
    subtitle: 'Taille des équipes et salaires par manager',
  },
  '/assistant': {
    title: 'Assistant RH',
    subtitle: 'Questions/réponses sur l’entrepôt DW-Projet (RAG)',
  },
};

export default function Layout() {
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const meta = PAGE_META[location.pathname] ?? PAGE_META['/page1'];

  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  return (
    <div className="app-shell">
      <div
        className={`backdrop ${sidebarOpen ? 'visible' : ''}`}
        onClick={() => setSidebarOpen(false)}
      />

      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-brand">
          
          <div className="brand-text">
            <strong>HR BI Dashboard</strong>
            <span>DW-Projet · Analytics</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section-title">Analyse</div>
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
              >
                <span className="nav-icon">
                  <Icon size={18} />
                </span>
                {item.label}
              </NavLink>
            );
          })}

          <div className="nav-section-title">Assistant</div>
          <NavLink
            to={ASSISTANT_NAV_ITEM.to}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <span className="nav-icon">
              <ChatIcon size={18} />
            </span>
            {ASSISTANT_NAV_ITEM.label}
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <span className={`status-dot ${apiInfo.useMock ? 'mock' : ''}`} />
          {apiInfo.useMock ? 'Mode mock (données fictives)' : `Backend : ${apiInfo.baseUrl}`}
        </div>
      </aside>

      <div className="main-column">
        <header className="topbar">
          <button
            type="button"
            className="menu-toggle"
            onClick={() => setSidebarOpen((o) => !o)}
            aria-label="Ouvrir le menu"
          >
            <MenuIcon size={18} />
          </button>
          <div>
            <div className="topbar-title">{meta.title}</div>
            <div className="topbar-subtitle">{meta.subtitle}</div>
          </div>
          <div className="topbar-spacer" />
          {apiInfo.useMock && <span className="badge mock">MOCK</span>}
          {!apiInfo.useMock && <span className="badge">API {apiInfo.baseUrl}</span>}
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
