/* Page 1 — Analyse par Département
   GET /api/dashboard/page1?departmentId=&employmentType=&employmentStatus=&gender= */

import { useMemo, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, Cell,
} from 'recharts';
import { useDashboardData } from '../hooks/useDashboardData';
import { useReferenceData } from '../hooks/useReferenceData';
import {
  fetchPageOne,
  GENDER_OPTIONS,
  EMPLOYMENT_TYPE_OPTIONS,
  EMPLOYMENT_STATUS_OPTIONS,
} from '../api/dashboard';
import FilterBar from '../components/FilterBar';
import KpiCard from '../components/KpiCard';
import ChartCard from '../components/ChartCard';
import { PageSkeleton } from '../components/Loader';
import ErrorState from '../components/ErrorState';
import EmptyState from '../components/EmptyState';
import { DashboardTooltip, TruncatedTick, TruncatedYTick, GENDER_COLORS, PALETTE } from '../components/chartUtils';
import { UsersIcon, BanknoteIcon } from '../components/icons';
import { formatNumber, formatSalary } from '../utils/format';
import { sumBy, weightedAverage } from '../utils/derive';

const EMPTY_FILTERS = { departmentId: '', employmentType: '', employmentStatus: '', gender: '' };

export default function PageOne() {
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const ref = useReferenceData();

  const params = useMemo(
    () => ({
      departmentId: filters.departmentId || undefined,
      employmentType: filters.employmentType || undefined,
      employmentStatus: filters.employmentStatus || undefined,
      gender: filters.gender || undefined,
    }),
    [filters],
  );

  const { status, data, error, retry } = useDashboardData(fetchPageOne, params);

  const departmentOptions = useMemo(
    () =>
      (ref.data?.departments ?? []).map((d) => ({
        value: d.departmentId,
        label: d.departmentName,
      })),
    [ref.data],
  );

  // KPI calculés côté front à partir des données retournées
  const kpis = useMemo(() => {
    if (!data) return null;
    const totalEmployees = sumBy(data.employeesByDepartment, (d) => d.count);
    const averageSalary =
      weightedAverage(data.employeesByDepartment, data.averageSalaryByDepartment) ??
      data.kpi?.averageSalary ??
      0;
    return { totalEmployees, averageSalary };
  }, [data]);

  // Pivot de la distribution par genre pour un stacked bar
  const genderRows = useMemo(() => {
    if (!data) return [];
    const byDept = new Map();
    for (const row of data.genderDistribution) {
      if (!byDept.has(row.department)) byDept.set(row.department, { department: row.department, M: 0, F: 0 });
      byDept.get(row.department)[row.gender] = row.count;
    }
    return data.employeesByDepartment.map((d) => byDept.get(d.name)).filter(Boolean);
  }, [data]);

  const renderContent = () => {
    if (status === 'error') return <ErrorState error={error} onRetry={retry} />;
    if (status === 'loading') return <PageSkeleton kpiCount={2} charts={3} />;
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
            subtitle={`${data.employeesByDepartment.length} départements`}
          />
          <KpiCard
            icon={<BanknoteIcon size={22} />}
            color="var(--c4)"
            label="Salaire moyen"
            value={formatSalary(kpis.averageSalary)}
            subtitle="Moyenne pondérée par département"
          />
        </div>

        <div className="grid">
          <div className="span-6">
            <ChartCard title="Employés par département" subtitle="Nombre d'employés par département">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={data.employeesByDepartment} margin={{ top: 8, right: 16, left: 0, bottom: 56 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
                  <XAxis dataKey="name" tick={<TruncatedTick max={13} />} interval={0} height={70} tickLine={false} axisLine={{ stroke: 'var(--border)' }} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} width={40} />
                  <Tooltip content={<DashboardTooltip />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="count" name="Employés" radius={[5, 5, 0, 0]} maxBarSize={42}>
                    {data.employeesByDepartment.map((entry, i) => (
                      <Cell key={entry.name} fill={PALETTE[i % PALETTE.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-6">
            <ChartCard title="Salaire moyen par département" subtitle="Trié du plus élevé au plus bas">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart
                  data={[...data.averageSalaryByDepartment].sort((a, b) => b.value - a.value)}
                  layout="vertical"
                  margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" horizontal={false} />
                  <XAxis type="number" tickLine={false} axisLine={false} />
                  <YAxis type="category" dataKey="name" tick={<TruncatedYTick max={19} />} width={135} tickLine={false} axisLine={false} />
                  <Tooltip content={<DashboardTooltip valueFormatter={(v) => formatSalary(v)} />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Bar dataKey="value" name="Salaire moyen" fill="var(--c2)" radius={[0, 5, 5, 0]} maxBarSize={22} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-12">
            <ChartCard title="Répartition par genre" subtitle="Nombre d'employés (H / F) par département">
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={genderRows} margin={{ top: 8, right: 16, left: 0, bottom: 56 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
                  <XAxis dataKey="department" tick={<TruncatedTick max={13} />} interval={0} height={70} tickLine={false} axisLine={{ stroke: 'var(--border)' }} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} width={40} />
                  <Tooltip content={<DashboardTooltip />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Legend verticalAlign="top" height={32} />
                  <Bar dataKey="F" name="Femme" stackId="gender" fill={GENDER_COLORS.F} radius={[0, 0, 0, 0]} maxBarSize={42} />
                  <Bar dataKey="M" name="Homme" stackId="gender" fill={GENDER_COLORS.M} radius={[5, 5, 0, 0]} maxBarSize={42} />
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
          { key: 'departmentId', label: 'Département', value: filters.departmentId, options: departmentOptions },
          { key: 'employmentType', label: "Type d'emploi", value: filters.employmentType, options: EMPLOYMENT_TYPE_OPTIONS },
          { key: 'employmentStatus', label: "Statut d'emploi", value: filters.employmentStatus, options: EMPLOYMENT_STATUS_OPTIONS },
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
