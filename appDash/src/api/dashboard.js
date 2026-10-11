/* ============================================================
   API Dashboard : fonctions d'accès + couche de normalisation.
   Les normalisateurs tolèrent de légères variations de nommage
   du backend (casing, label vs name, etc.) et exposent une
   forme interne stable au reste de l'application.
   ============================================================ */

import { apiFetch } from './client';

/* ---------- Options de filtres statiques ---------- */

/*
 * NOTE : le backend n'expose PAS les colonnes Gender / EmploymentType /
 * EmploymentStatus dans GET /api/dashboard/employees (seuls employeeId,
 * companyId, firstName, lastName, email, departmentId, jobTitleId,
 * baseSalary, isActive sont retournés). Les valeurs ci-dessous ont été
 * vérifiées directement contre l'API :
 *  - gender accepte "M" / "F"  ("Male"/"Female" renvoient une réponse vide)
 *  - employmentType : "Full-Time" / "Part-Time" (issu de employmentTypeDistribution)
 *  - employmentStatus : "Active" fonctionne ("Inactive" renvoie 0 employé)
 */
export const GENDER_OPTIONS = [
  { value: 'M', label: 'Homme' },
  { value: 'F', label: 'Femme' },
];

export const EMPLOYMENT_TYPE_OPTIONS = [
  { value: 'Full-Time', label: 'Full-Time' },
  { value: 'Part-Time', label: 'Part-Time' },
];

export const EMPLOYMENT_STATUS_OPTIONS = [
  { value: 'Active', label: 'Active' },
  { value: 'Inactive', label: 'Inactive' },
];

/* ---------- Helpers de normalisation ---------- */

const asArray = (v) => (Array.isArray(v) ? v : []);
const toNumber = (v, fallback = 0) =>
  typeof v === 'number' && Number.isFinite(v) ? v : Number.isFinite(Number(v)) && v !== null && v !== undefined && v !== '' ? Number(v) : fallback;
const toString = (v) => (typeof v === 'string' ? v : v == null ? '' : String(v));

function normalizeNameCount(list) {
  return asArray(list).map((item) => ({
    name: toString(item.name ?? item.label ?? item.departmentName ?? item.jobTitle ?? item.companyName ?? item.managerName),
    count: toNumber(item.count ?? item.total ?? item.employeeCount),
  }));
}

function normalizeNameValue(list) {
  return asArray(list).map((item) => ({
    name: toString(item.name ?? item.label ?? item.departmentName ?? item.jobTitle ?? item.companyName),
    value: toNumber(item.value ?? item.averageSalary ?? item.avgSalary),
  }));
}

function normalizePageOne(raw) {
  const r = raw || {};
  const kpi = r.kpi || {};
  return {
    employeesByDepartment: normalizeNameCount(r.employeesByDepartment),
    averageSalaryByDepartment: normalizeNameValue(r.averageSalaryByDepartment),
    genderDistribution: asArray(r.genderDistribution).map((item) => ({
      department: toString(item.department ?? item.departmentName ?? item.name),
      gender: toString(item.gender ?? item.sexe),
      count: toNumber(item.count),
    })),
    kpi: {
      totalEmployees: toNumber(kpi.totalEmployees ?? kpi.total),
      averageSalary: toNumber(kpi.averageSalary ?? kpi.average),
      maxSalary: kpi.maxSalary ?? null,
      minSalary: kpi.minSalary ?? null,
      totalSalaryMass: kpi.totalSalaryMass ?? null,
    },
  };
}

function normalizePageTwo(raw) {
  const r = raw || {};
  const kpi = r.kpi || {};
  return {
    employeesByJobTitle: normalizeNameCount(r.employeesByJobTitle),
    averageSalaryByJobTitle: normalizeNameValue(r.averageSalaryByJobTitle),
    employmentTypeDistribution: asArray(r.employmentTypeDistribution).map((item) => ({
      name: toString(item.employmentType ?? item.type ?? item.name),
      count: toNumber(item.count),
    })),
    kpi: {
      totalEmployees: toNumber(kpi.totalEmployees ?? kpi.total),
      averageSalary: toNumber(kpi.averageSalary ?? kpi.average),
      maxSalary: kpi.maxSalary ?? null,
      minSalary: kpi.minSalary ?? null,
      totalSalaryMass: kpi.totalSalaryMass ?? null,
    },
  };
}

function normalizePageThree(raw) {
  const r = raw || {};
  return {
    companiesData: asArray(r.companiesData).map((item) => ({
      companyName: toString(item.companyName ?? item.name),
      employeeCount: toNumber(item.employeeCount ?? item.count ?? item.totalEmployees),
      averageSalary: toNumber(item.averageSalary ?? item.value),
      totalSalaryMass: toNumber(item.totalSalaryMass ?? item.salaryMass),
    })),
    employeesByCompany: normalizeNameCount(r.employeesByCompany),
    averageSalaryByCompany: normalizeNameValue(r.averageSalaryByCompany),
  };
}

function normalizePageFour(raw) {
  const r = raw || {};
  const stats = r.departmentStats || {};
  return {
    managers: asArray(r.managers).map((item) => ({
      managerId: item.managerId ?? null,
      managerName: toString(item.managerName ?? item.name),
      subordinateCount: toNumber(item.subordinateCount ?? item.count),
      averageSalary: toNumber(item.averageSalary),
    })),
    employeesByManager: normalizeNameCount(r.employeesByManager),
    averageSalaryByManager: normalizeNameValue(r.averageSalaryByManager),
    departmentStats: {
      totalDepartments: toNumber(stats.totalDepartments ?? stats.activeDepartments ?? stats.departmentCount),
    },
  };
}

/* ---------- Résolution des noms de managers ---------- */

/**
 * Le backend page4 retourne des libellés "Manager <id>" dans
 * employeesByManager / averageSalaryByManager, et managerId = null dans
 * managers. On résout les vrais noms via la liste des employés.
 */
export function resolveManagerLabel(label, employees = []) {
  const match = /^Manager\s+(\d+)$/i.exec(toString(label).trim());
  if (!match) return toString(label);
  const emp = employees.find((e) => Number(e.employeeId) === Number(match[1]));
  return emp ? `${emp.firstName} ${emp.lastName}` : toString(label);
}

/**
 * Options du filtre "Manager" : le backend ne retournant pas managerId,
 * on associe chaque nom de manager à son employeeId (correspondance
 * "Prénom Nom" sur /api/dashboard/employees). En mode mock, managerId
 * est présent directement.
 */
export function resolveManagerOptions(page4Data, employees = []) {
  if (!page4Data) return [];
  const options = [];
  for (const manager of page4Data.managers || []) {
    if (manager.managerId != null) {
      options.push({ value: manager.managerId, label: manager.managerName });
      continue;
    }
    const emp = employees.find(
      (e) => `${e.firstName} ${e.lastName}`.trim().toLowerCase() === manager.managerName.trim().toLowerCase(),
    );
    if (emp) {
      options.push({ value: emp.employeeId, label: manager.managerName });
    } else {
      console.warn(`[Dashboard] manager introuvable dans /employees : ${manager.managerName}`);
    }
  }
  return options;
}

/* ---------- Endpoints ---------- */

export const fetchCompanies = (signal) => apiFetch('/api/dashboard/companies', { signal });
export const fetchDepartments = (signal) => apiFetch('/api/dashboard/departements', { signal });
export const fetchJobs = (signal) => apiFetch('/api/dashboard/jobs', { signal });
export const fetchEmployees = (signal) => apiFetch('/api/dashboard/employees', { signal });

export function fetchPageOne(params, signal) {
  return apiFetch('/api/dashboard/page1', { params, signal }).then(normalizePageOne);
}

export function fetchPageTwo(params, signal) {
  return apiFetch('/api/dashboard/page2', { params, signal }).then(normalizePageTwo);
}

export function fetchPageThree(params, signal) {
  return apiFetch('/api/dashboard/page3', { params, signal }).then(normalizePageThree);
}

export function fetchPageFour(params, signal) {
  return apiFetch('/api/dashboard/page4', { params, signal }).then(normalizePageFour);
}
