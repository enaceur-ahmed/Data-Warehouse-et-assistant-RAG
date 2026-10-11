/* Page 3 — Analyse par Société
   GET /api/dashboard/page3?companyId=&departmentId=&jobTitleId= */

import { useMemo, useState } from 'react';
import {
  ComposedChart, Bar, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, Cell,
} from 'recharts';
import { useDashboardData } from '../hooks/useDashboardData';
import { useReferenceData } from '../hooks/useReferenceData';
import { fetchPageThree, GENDER_OPTIONS } from '../api/dashboard';
import FilterBar from '../components/FilterBar';
import KpiCard from '../components/KpiCard';
import ChartCard from '../components/ChartCard';
import DataTable from '../components/DataTable';
import { PageSkeleton } from '../components/Loader';
import ErrorState from '../components/ErrorState';
import EmptyState from '../components/EmptyState';
import { DashboardTooltip, TruncatedTick, PALETTE } from '../components/chartUtils';
import {
  BuildingIcon, UsersIcon, BanknoteIcon, ActivityIcon,
} from '../components/icons';
import { formatNumber, formatSalary } from '../utils/format';
import { sumBy } from '../utils/derive';

const EMPTY_FILTERS = { companyId: '', departmentId: '', jobTitleId: '' };

export default function PageThree() {
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const ref = useReferenceData();

  const params = useMemo(
    () => ({
      companyId: filters.companyId || undefined,
      departmentId: filters.departmentId || undefined,
      jobTitleId: filters.jobTitleId || undefined,
    }),
    [filters],
  );

  const { status, data, error, retry } = useDashboardData(fetchPageThree, params);

  const companyOptions = useMemo(
    () =>
      (ref.data?.companies ?? []).map((c) => ({
        value: c.companyId,
        label: c.companyName,
      })),
    [ref.data],
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

  // KPI calculés côté front à partir de companiesData
  const kpis = useMemo(() => {
    if (!data) return null;
    const companyCount = data.companiesData.length;
    const totalEmployees = sumBy(data.companiesData, (c) => c.employeeCount);
    const totalSalaryMass = sumBy(data.companiesData, (c) => c.totalSalaryMass);
    const averageSalary = totalEmployees > 0 ? totalSalaryMass / totalEmployees : 0;
    return { companyCount, totalEmployees, totalSalaryMass, averageSalary };
  }, [data]);

  const chartRows = useMemo(() => {
    if (!data) return [];
    return data.companiesData.map((c) => ({
      name: c.companyName,
      effectifs: c.employeeCount,
      salaireMoyen: Number(c.averageSalary.toFixed(2)),
    }));
  }, [data]);

  const renderContent = () => {
    if (status === 'error') return <ErrorState error={error} onRetry={retry} />;
    if (status === 'loading') return <PageSkeleton kpiCount={4} charts={2} />;
    if (!data || kpis.totalEmployees === 0) {
      return <EmptyState title="Aucune donnée" />;
    }

    return (
      <>
        <div className="kpi-row">
          <KpiCard
            icon={<BuildingIcon size={22} />}
            color="var(--c6)"
            label="Sociétés"
            value={formatNumber(kpis.companyCount)}
            subtitle="Avec des employés"
          />
          <KpiCard
            icon={<UsersIcon size={22} />}
            color="var(--c1)"
            label="Total des employés"
            value={formatNumber(kpis.totalEmployees)}
            subtitle={`${data.employeesByCompany.length} sociétés`}
          />
          <KpiCard
            icon={<BanknoteIcon size={22} />}
            color="var(--c4)"
            label="Masse salariale"
            value={formatSalary(kpis.totalSalaryMass)}
            subtitle="Somme des salaires de base"
          />
          <KpiCard
            icon={<ActivityIcon size={22} />}
            color="var(--c3)"
            label="Salaire moyen"
            value={formatSalary(kpis.averageSalary)}
            subtitle="Masse salariale / effectif"
          />
        </div>

        <div className="grid">
          <div className="span-12">
            <ChartCard
              title="Effectifs et salaire moyen par société"
              subtitle="Barres = nombre d'employés (axe gauche) · Courbe = salaire moyen (axe droit)"
            >
              <ResponsiveContainer width="100%" height={320}>
                <ComposedChart data={chartRows} margin={{ top: 8, right: 24, left: 0, bottom: 40 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
                  <XAxis dataKey="name" tick={<TruncatedTick max={18} />} interval={0} height={64} tickLine={false} axisLine={{ stroke: 'var(--border)' }} />
                  <YAxis yAxisId="left" allowDecimals={false} tickLine={false} axisLine={false} width={44} />
                  <YAxis yAxisId="right" orientation="right" tickLine={false} axisLine={false} width={56} />
                  <Tooltip content={<DashboardTooltip />} cursor={{ fill: 'rgba(79, 70, 229, 0.06)' }} />
                  <Legend verticalAlign="top" height={32} />
                  <Bar yAxisId="left" dataKey="effectifs" name="Employés" radius={[5, 5, 0, 0]} maxBarSize={70}>
                    {chartRows.map((entry, i) => (
                      <Cell key={entry.name} fill={PALETTE[i % PALETTE.length]} />
                    ))}
                  </Bar>
                  <Line
                    yAxisId="right"
                    type="monotone"
                    dataKey="salaireMoyen"
                    name="Salaire moyen"
                    stroke="var(--warning)"
                    strokeWidth={2.5}
                    dot={{ r: 4, fill: 'var(--warning)', strokeWidth: 0 }}
                    activeDot={{ r: 6 }}
                  />
                </ComposedChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <div className="span-12">
            <DataTable
              columns={[
                { key: 'companyName', header: 'Société' },
                {
                  key: 'employeeCount',
                  header: 'Employés',
                  numeric: true,
                  render: (v) => formatNumber(v),
                },
                {
                  key: 'averageSalary',
                  header: 'Salaire moyen',
                  numeric: true,
                  render: (v) => formatSalary(v),
                },
                {
                  key: 'totalSalaryMass',
                  header: 'Masse salariale',
                  numeric: true,
                  render: (v) => formatSalary(v),
                },
              ]}
              rows={data.companiesData.map((c) => ({ id: c.companyName, ...c }))}
              defaultSortKey="employeeCount"
              defaultSortDir="desc"
              searchPlaceholder="Rechercher une société…"
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
          { key: 'companyId', label: 'Société', value: filters.companyId, options: companyOptions },
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
