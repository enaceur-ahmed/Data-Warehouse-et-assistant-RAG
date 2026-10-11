/* Page 4 — Hiérarchie / Managers
   GET /api/dashboard/page4?managerId=&departmentId=&jobTitleId=

   Particularités du backend (vraies données) :
   - managers[].managerId est toujours null → l'ID du manager est
     retrouvé par correspondance de nom avec /api/dashboard/employees.
   - employeesByManager / averageSalaryByManager utilisent des
     libellés "Manager <id>" → résolus en vrais noms via les employés.
   - managers[].averageSalary vaut toujours 0 → on utilise la moyenne
     issue de averageSalaryByManager (jointure par nom résolu).
   - Incohérence backend : employeesByManager contient un fantôme
     "Manager 0" (aucun employé avec l'ID 0) en plus des 8 vrais
     managers. Les graphiques reflètent les données telles quelles ;
     la table utilise la liste managers (8 entrées) et l'KPI
     "employés gérés" est la somme de employeesByManager (= effectif
     total, cohérent avec la page 1).
*/

import { useMemo, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell,
} from 'recharts';
import { useDashboardData } from '../hooks/useDashboardData';
import { useReferenceData } from '../hooks/useReferenceData';
import {
  fetchPageFour,
  resolveManagerOptions,
  resolveManagerLabel,
} from '../api/dashboard';
import FilterBar from '../components/FilterBar';
import KpiCard from '../components/KpiCard';
import ChartCard from '../components/ChartCard';
import DataTable from '../components/DataTable';
import { PageSkeleton } from '../components/Loader';
import ErrorState from '../components/ErrorState';
import EmptyState from '../components/EmptyState';
import { DashboardTooltip, TruncatedYTick, PALETTE } from '../components/chartUtils';
import {
  UsersIcon, UserIcon, LayersIcon, BanknoteIcon,
} from '../components/icons';
import { formatNumber, formatSalary } from '../utils/format';
import { sumBy, averageOf } from '../utils/derive';

const EMPTY_FILTERS = { managerId: '', departmentId: '', jobTitleId: '' };

export default function PageFour() {
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const ref = useReferenceData();

  const params = useMemo(
    () => ({
      managerId: filters.managerId || undefined,
      departmentId: filters.departmentId || undefined,
      jobTitleId: filters.jobTitleId || undefined,
    }),
    [filters],
  );

  // Requête principale (filtres courants)
  const { status, data, error, retry } = useDashboardData(fetchPageFour, params);

  // Chargement initial SANS filtre pour alimenter le dropdown "Manager"
  const { data: allManagersData } = useDashboardData(fetchPageFour, {}, { debounceMs: 0 });

  const employees = ref.data?.employees ?? [];

  // Options du filtre Manager (depuis la réponse page4 non filtrée)
  const managerOptions = useMemo(
    () => resolveManagerOptions(allManagersData, employees),
    [allManagersData, employees],
  );

  const departmentOptions = useMemo(
    () =>
      (ref.data?.departments ?? []).map((d) => ({
        value: d.departmentId,
        label: d.departmentName,
      })),
    [ref.data],
  );

  const jobOptions = useMemo(
    () =>
      (ref.data?.jobs ?? []).map((j) => ({ value: j.jobTitleId, label: j.jobTitle })),
    [ref.data],
  );

  // Lignes de managers enrichies : noms résolus + vrai salaire moyen
  // issu de averageSalaryByManager (le champ averageSalary de
  // managers[] étant toujours 0 côté backend).
  const managerRows = useMemo(() => {
    if (!data) return [];
    const avgByName = new Map(
      data.averageSalaryByManager.map((m) => [
        resolveManagerLabel(m.name, employees).toLowerCase(),
        m.value,
      ]),
    );
    return (data.managers || []).map((m) => ({
      id: m.managerName,
      name: m.managerName,
      subordinateCount: m.subordinateCount,
      averageSalary:
        avgByName.get(m.managerName.toLowerCase()) ?? m.averageSalary,
    }));
  }, [data, employees]);

  // Graphiques : libellés "Manager N" résolus en vrais noms
  const employeesByManagerRows = useMemo(() => {
    if (!data) return [];
    return data.employeesByManager.map((m) => ({
      name: resolveManagerLabel(m.name, employees),
      count: m.count,
    }));
  }, [data, employees]);

  const avgSalaryByManagerRows = useMemo(() => {
    if (!data) return [];
    return [...data.averageSalaryByManager]
      .map((m) => ({
        name: resolveManagerLabel(m.name, employees),
        value: m.value,
      }))
      .sort((a, b) => b.value - a.value);
  }, [data, employees]);

  // KPI calculés côté front
  const kpis = useMemo(() => {
    if (!data) return null;
    const managerCount = (data.managers || []).length;
    const totalManaged = sumBy(data.employeesByManager, (m) => m.count);
    const avgTeamSize = managerCount > 0 ? totalManaged / managerCount : 0;
    const activeDepartments = data.departmentStats.totalDepartments;
    return { managerCount, avgTeamSize, activeDepartments, totalManaged };
  }, [data]);

  const renderContent = () => {
    if (status === 'error') return <ErrorState error={error} onRetry={retry} />;
    if (status === 'loading') return <PageSkeleton kpiCount={4} charts={2} />;
    if (!data || kpis.totalManaged === 0) {
      return <EmptyState title="Aucune donnée" />;
    }

    return (
      <>
        <div className="kpi-row">
          <KpiCard
            icon={<UsersIcon size={22} />}
            color="var(--c1)"
            label="Managers"
            value={formatNumber(kpis.managerCount)}
            subtitle="Avec des subordonnés"
          />
          <KpiCard
            icon={<UserIcon size={22} />}
            color="var(--c4)"
            label="Taille moyenne d'équipe"
            value={formatNumber(kpis.avgTeamSize, { decimals: 1 })}
            subtitle="Employés gérés par manager"
          />
          <KpiCard
            icon={<LayersIcon size={22} />}
            color="var(--c6)"
            label="Départements actifs"
            value={formatNumber(kpis.activeDepartments)}
            subtitle="Selon le backend"
          />
          <KpiCard
            icon={<BanknoteIcon size={22} />}
            color="var(--c3)"
            label="Employés gérés"
            value={formatNumber(kpis.totalManaged)}
            subtitle="Total des subordonnés"
          />
        </div>

        <div className="grid">
          <div className="span-6">
            <ChartCard title="Employés par manager" subtitle="Nombre de subordonnés par manager">
              <ResponsiveContainer width="100%" height={340}>
                <BarChart
                  data={[...employeesByManagerRows].sort((a, b) => b.count - a.count)}
                  layout="vertical"
                  margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" horizontal={false} />
                  <XAxis type="number" allowDecimals={false} tickLine={false} axisLine={false} />
                  <YAxis type="category" dataKey="name" tick={<TruncatedYTick max={18} />} width={140} tickLine={false} axisLine={false} />
                  <Tooltip content={<DashboardTooltip />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="count" name="Subordonnés" radius={[0, 5, 5, 0]} maxBarSize={22}>
                    {employeesByManagerRows.map((entry, i) => (
                      <Cell key={entry.name} fill={PALETTE[i % PALETTE.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-6">
            <ChartCard title="Salaire moyen par équipe" subtitle="Salaire moyen des subordonnés, par manager">
              <ResponsiveContainer width="100%" height={340}>
                <BarChart
                  data={avgSalaryByManagerRows}
                  layout="vertical"
                  margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" horizontal={false} />
                  <XAxis type="number" tickLine={false} axisLine={false} />
                  <YAxis type="category" dataKey="name" tick={<TruncatedYTick max={18} />} width={140} tickLine={false} axisLine={false} />
                  <Tooltip content={<DashboardTooltip valueFormatter={(v) => formatSalary(v)} />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="value" name="Salaire moyen" fill="var(--c7)" radius={[0, 5, 5, 0]} maxBarSize={22} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-12">
            <DataTable
              columns={[
                { key: 'name', header: 'Manager' },
                {
                  key: 'subordinateCount',
                  header: 'Subordonnés',
                  numeric: true,
                  render: (v) => formatNumber(v),
                },
                {
                  key: 'averageSalary',
                  header: 'Salaire moyen de l\'équipe',
                  numeric: true,
                  render: (v) => formatSalary(v),
                },
              ]}
              rows={managerRows}
              defaultSortKey="subordinateCount"
              defaultSortDir="desc"
              searchPlaceholder="Rechercher un manager…"
            />
          </div>
        </div>
      </>
    );
  };

  return (
    <div>
      <FilterBar
        filters={[
          { key: 'managerId', label: 'Manager', value: filters.managerId, options: managerOptions },
          { key: 'departmentId', label: 'Département', value: filters.departmentId, options: departmentOptions },
          { key: 'jobTitleId', label: 'Poste', value: filters.jobTitleId, options: jobOptions },
        ]}
        onReset={() => setFilters(EMPTY_FILTERS)}
        onChange={(key, value) => setFilters((f) => ({ ...f, [key]: value }))}
        disabled={status === 'loading'}
      />
      {ref.status === 'error' && (
        <p className="muted-text page-section" role="note">
          ⚠ Options de filtrage indisponibles : {ref.error?.message}
        </p>
      )}
      {renderContent()}
    </div>
  );
}
