/* Page 2 — Analyse par Poste
   GET /api/dashboard/page2?jobTitleId=&employmentType=&gender= */

import { useMemo, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, Cell, PieChart, Pie,
} from 'recharts';
import { useDashboardData } from '../hooks/useDashboardData';
import { useReferenceData } from '../hooks/useReferenceData';
import {
  fetchPageTwo,
  GENDER_OPTIONS,
  EMPLOYMENT_TYPE_OPTIONS,
} from '../api/dashboard';
import FilterBar from '../components/FilterBar';
import KpiCard from '../components/KpiCard';
import ChartCard from '../components/ChartCard';
import { PageSkeleton } from '../components/Loader';
import ErrorState from '../components/ErrorState';
import EmptyState from '../components/EmptyState';
import {
  DashboardTooltip, TruncatedTick, TruncatedYTick, EMPLOYMENT_COLORS, PALETTE,
} from '../components/chartUtils';
import {
  UsersIcon, BanknoteIcon, TrendingUpIcon, TrendingDownIcon,
} from '../components/icons';
import { formatNumber, formatSalary } from '../utils/format';
import { sumBy, weightedAverage, maxBy, minBy } from '../utils/derive';

const EMPTY_FILTERS = { jobTitleId: '', employmentType: '', gender: '' };

export default function PageTwo() {
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const ref = useReferenceData();

  const params = useMemo(
    () => ({
      jobTitleId: filters.jobTitleId || undefined,
      employmentType: filters.employmentType || undefined,
      gender: filters.gender || undefined,
    }),
    [filters],
  );

  const { status, data, error, retry } = useDashboardData(fetchPageTwo, params);

  const jobOptions = useMemo(
    () =>
      (ref.data?.jobs ?? []).map((j) => ({ value: j.jobTitleId, label: j.jobTitle })),
    [ref.data],
  );

  // KPI calculés côté front : total & moyenne pondérée issus des agrégats,
  // max/min : champs kpi du backend (vrais min/max individuels), avec
  // repli sur les moyennes par poste si absents.
  const kpis = useMemo(() => {
    if (!data) return null;
    const totalEmployees = sumBy(data.employeesByJobTitle, (d) => d.count);
    const averageSalary =
      weightedAverage(data.employeesByJobTitle, data.averageSalaryByJobTitle) ??
      data.kpi?.averageSalary ??
      0;
    const maxSalary =
      data.kpi?.maxSalary ?? maxBy(data.averageSalaryByJobTitle, (d) => d.value);
    const minSalary =
      data.kpi?.minSalary ?? minBy(data.averageSalaryByJobTitle, (d) => d.value);
    return { totalEmployees, averageSalary, maxSalary, minSalary };
  }, [data]);

  const employmentRows = useMemo(() => {
    if (!data) return [];
    return data.employmentTypeDistribution.map((d) => ({
      name: d.name,
      value: d.count,
    }));
  }, [data]);

  const renderContent = () => {
    if (status === 'error') return <ErrorState error={error} onRetry={retry} />;
    if (status === 'loading') return <PageSkeleton kpiCount={4} charts={3} />;
    if (!data || kpis.totalEmployees === 0) {
      return <EmptyState title="Aucune donnée" />;
    }

    return (
      <>
        <div className="kpi-row">
          <KpiCard
            icon={<UsersIcon size={22} />}
            color="var(--c1)"
            label="Total des employés"
            value={formatNumber(kpis.totalEmployees)}
            subtitle={`${data.employeesByJobTitle.length} postes`}
          />
          <KpiCard
            icon={<BanknoteIcon size={22} />}
            color="var(--c4)"
            label="Salaire moyen"
            value={formatSalary(kpis.averageSalary)}
            subtitle="Moyenne pondérée par poste"
          />
          <KpiCard
            icon={<TrendingUpIcon size={22} />}
            color="var(--c3)"
            label="Salaire max"
            value={formatSalary(kpis.maxSalary)}
            subtitle="Plus haut salaire individuel"
          />
          <KpiCard
            icon={<TrendingDownIcon size={22} />}
            color="var(--c9)"
            label="Salaire min"
            value={formatSalary(kpis.minSalary)}
            subtitle="Plus bas salaire individuel"
          />
        </div>

        <div className="grid">
          <div className="span-6">
            <ChartCard title="Employés par poste" subtitle="Nombre d'employés par titre de poste">
              <ResponsiveContainer width="100%" height={420}>
                <BarChart
                  data={[...data.employeesByJobTitle].sort((a, b) => b.count - a.count)}
                  layout="vertical"
                  margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" horizontal={false} />
                  <XAxis type="number" allowDecimals={false} tickLine={false} axisLine={false} />
                  <YAxis type="category" dataKey="name" tick={<TruncatedYTick max={20} />} width={150} tickLine={false} axisLine={false} />
                  <Tooltip content={<DashboardTooltip />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="count" name="Employés" radius={[0, 5, 5, 0]} maxBarSize={20}>
                    {data.employeesByJobTitle.map((entry, i) => (
                      <Cell key={entry.name} fill={PALETTE[i % PALETTE.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-6">
            <ChartCard title="Salaire moyen par poste" subtitle="Trié du plus élevé au plus bas">
              <ResponsiveContainer width="100%" height={420}>
                <BarChart
                  data={[...data.averageSalaryByJobTitle].sort((a, b) => b.value - a.value)}
                  layout="vertical"
                  margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" horizontal={false} />
                  <XAxis type="number" tickLine={false} axisLine={false} />
                  <YAxis type="category" dataKey="name" tick={<TruncatedYTick max={20} />} width={150} tickLine={false} axisLine={false} />
                  <Tooltip content={<DashboardTooltip valueFormatter={(v) => formatSalary(v)} />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="value" name="Salaire moyen" fill="var(--c6)" radius={[0, 5, 5, 0]} maxBarSize={20} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-6">
            <ChartCard title="Type d'emploi" subtitle="Répartition des employés par type de contrat">
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={employmentRows}
                    dataKey="value"
                    nameKey="name"
                    innerRadius="58%"
                    outerRadius="85%"
                    paddingAngle={3}
                    strokeWidth={2}
                    label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)} %)`}
                  >
                    {employmentRows.map((entry) => (
                      <Cell key={entry.name} fill={EMPLOYMENT_COLORS[entry.name] ?? PALETTE[employmentRows.indexOf(entry) % PALETTE.length]} />
                    ))}
                  </Pie>
                  <Tooltip content={<DashboardTooltip valueFormatter={(v) => formatNumber(v)} />} />
                  <Legend verticalAlign="bottom" height={32} />
                </PieChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-6">
            <ChartCard title="Effectif par poste (détail)" subtitle="Vue alternative — barres verticales">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart
                  data={[...data.employeesByJobTitle].sort((a, b) => b.count - a.count)}
                  margin={{ top: 8, right: 16, left: 0, bottom: 90 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
                  <XAxis dataKey="name" tick={<TruncatedTick max={9} />} interval={0} height={100} tickLine={false} axisLine={{ stroke: 'var(--border)' }} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} width={40} />
                  <Tooltip content={<DashboardTooltip />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="count" name="Employés" fill="var(--c2)" radius={[5, 5, 0, 0]} maxBarSize={30} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>
        </div>
      </>
    );
  };

  return (
    <div>
      <FilterBar
        filters={[
          { key: 'jobTitleId', label: 'Poste', value: filters.jobTitleId, options: jobOptions },
          { key: 'employmentType', label: "Type d'emploi", value: filters.employmentType, options: EMPLOYMENT_TYPE_OPTIONS },
          { key: 'gender', label: 'Genre', value: filters.gender, options: GENDER_OPTIONS },
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
