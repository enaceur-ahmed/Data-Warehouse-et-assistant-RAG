# HR BI Dashboard

Frontend React 18 (JSX) + Vite pour le data warehouse RH **DW-Projet**
(backend Spring Boot 4 / Java 21 sur `http://localhost:8080`).

## Fonctionnalités (4 pages d'analyse)

| Route | Page | Endpoint backend |
| --- | --- | --- |
| `/page1` | Départements | `GET /api/dashboard/page1` |
| `/page2` | Postes | `GET /api/dashboard/page2` |
| `/page3` | Sociétés | `GET /api/dashboard/page3` |
| `/page4` | Managers | `GET /api/dashboard/page4` |

- Filtres par page (dropdowns « Tous » inclus, reset, debounce 300 ms,
  annulation des requêtes périmées via `AbortController`)
- KPI cards calculés côté front uniquement à partir des données retournées
- Graphiques Recharts (barres, stacked bars, donut, composed chart)
- Tableaux triables + recherche (page 3 et 4)
- États : squelettes de chargement, erreur avec « Réessayer », vide
- Thème clair, CSS variables, responsive (laptop / tablette)

## Prérequis

- Node.js 18+ (testé avec Node 22)
- Backend Spring Boot démarré sur `http://localhost:8080`
  (CORS déjà autorisé pour `http://localhost:5173`)

## Lancer

```powershell
cd "C:\Users\HP\Downloads\demo (1)\appDash"
npm install
npm run dev        # http://localhost:5173
npm run build      # build de production dans dist/
```

## Mode mock (sans le backend)

Créez un `.env` (ou modifiez-le) :

```
VITE_USE_MOCK=true
```

puis redémarrez `npm run dev`. Les données fictives respectent exactement
les formes JSON du backend et les filtres sont simulés.

## Configuration

| Variable | Défaut | Rôle |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL du backend |
| `VITE_USE_MOCK` | `false` | `true` = données fictives |

## Structure

```
src/
├── api/         client.js (fetch + AbortController), dashboard.js
│                (endpoints + normalisation), mock.js (moteur de données)
├── components/  Layout, FilterBar, KpiCard, ChartCard, DataTable,
│                Loader, ErrorState, EmptyState, chartUtils, icons
├── hooks/       useDashboardData (debounce/abort/retry),
│                useReferenceData (cache global)
├── pages/       PageOne.jsx … PageFour.jsx
├── styles/      variables.css, base.css, layout.css, components.css
└── utils/       format.js (fr-FR), derive.js (KPI calculés)
```

## Notes backend observées (voir le rapport final pour le détail)

- `gender` accepte `M`/`F` uniquement (`Male`/`Female` → réponse vide)
- `/api/dashboard/employees` n'expose pas Gender / EmploymentType /
  EmploymentStatus → options de ces filtres statiques (valeurs vérifiées)
- `page4.managers[].managerId` est toujours `null` → l'ID du manager est
  retrouvé par correspondance de nom avec `/employees`
- `page4.managers[].averageSalary` vaut toujours `0` → la moyenne affichée
  vient de `averageSalaryByManager`
- Le service backend avale les exceptions et renvoie des réponses vides :
  la page affiche un état « Aucune donnée / vérifiez le backend »
