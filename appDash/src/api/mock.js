/* ============================================================
   Mode mock (VITE_USE_MOCK=true)
   Données fictives réalistes respectant EXACTEMENT les formes
   JSON retournées par le backend Spring Boot (vérifiées en live).
   Génération déterministe (graine fixe) + filtrage côté serveur
   simulé pour que les filtres fonctionnent comme en mode réel.
   ============================================================ */

function mulberry32(a) {
  return function () {
    a |= 0;
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const rand = mulberry32(20240607);
const ri = (min, max) => min + Math.floor(rand() * (max - min + 1));
const pick = (arr) => arr[Math.floor(rand() * arr.length)];
const round50 = (n) => Math.round(n / 50) * 50;

/* ---------- Données de référence ---------- */

const companies = [
  { companyId: 1, companyCode: 'DASH', companyName: 'DashMaster Tunisia', taxId: 'TN1234567890', address: '45 Avenue de la Liberté', city: 'Tunis', country: 'Tunisia', phone: '+216 71 123 456', email: 'contact@dashmaster.tn', website: 'www.dashmaster.tn', isActive: true },
  { companyId: 2, companyCode: 'TECH', companyName: 'TechSolutions SARL', taxId: 'TN9876543210', address: '12 Rue du Lac', city: 'Ariana', country: 'Tunisia', phone: '+216 71 234 567', email: 'info@techsolutions.tn', website: 'www.techsolutions.tn', isActive: true },
  { companyId: 3, companyCode: 'NOVA', companyName: 'NovaSoft', taxId: 'TN1122334455', address: '8 Rue Ibn Khaldoun', city: 'Sfax', country: 'Tunisia', phone: '+216 74 555 789', email: 'contact@novasoft.tn', website: 'www.novasoft.tn', isActive: true },
];

const departments = [
  { departmentId: 1, companyId: 1, departmentCode: 'EXEC', departmentName: 'Executive Management', isActive: true },
  { departmentId: 2, companyId: 1, departmentCode: 'SALES', departmentName: 'Sales & Marketing', isActive: true },
  { departmentId: 3, companyId: 1, departmentCode: 'IT', departmentName: 'Information Technology', isActive: true },
  { departmentId: 4, companyId: 1, departmentCode: 'HR', departmentName: 'Human Resources', isActive: true },
  { departmentId: 5, companyId: 1, departmentCode: 'FIN', departmentName: 'Finance & Accounting', isActive: true },
  { departmentId: 6, companyId: 1, departmentCode: 'CS', departmentName: 'Customer Service', isActive: true },
  { departmentId: 7, companyId: 1, departmentCode: 'R&D', departmentName: 'Research & Development', isActive: true },
  { departmentId: 8, companyId: 1, departmentCode: 'LOG', departmentName: 'Logistics & Warehouse', isActive: true },
  { departmentId: 9, companyId: 2, departmentCode: 'DEV', departmentName: 'Software Engineering', isActive: true },
  { departmentId: 10, companyId: 2, departmentCode: 'SUP', departmentName: 'Customer Success', isActive: true },
  { departmentId: 11, companyId: 3, departmentCode: 'PRO', departmentName: 'Product', isActive: true },
  { departmentId: 12, companyId: 3, departmentCode: 'OPS', departmentName: 'Operations Sfax', isActive: true },
];

const jobs = [
  { jobTitleId: 1, jobTitle: 'Chief Executive Officer', jobLevel: 10, minSalary: 15000, maxSalary: 25000 },
  { jobTitleId: 2, jobTitle: 'Chief Financial Officer', jobLevel: 10, minSalary: 12000, maxSalary: 20000 },
  { jobTitleId: 3, jobTitle: 'Chief Technology Officer', jobLevel: 10, minSalary: 12000, maxSalary: 20000 },
  { jobTitleId: 4, jobTitle: 'Director', jobLevel: 9, minSalary: 8000, maxSalary: 15000 },
  { jobTitleId: 5, jobTitle: 'Manager', jobLevel: 8, minSalary: 5000, maxSalary: 10000 },
  { jobTitleId: 6, jobTitle: 'Team Lead', jobLevel: 7, minSalary: 4000, maxSalary: 7000 },
  { jobTitleId: 7, jobTitle: 'Senior Developer', jobLevel: 6, minSalary: 3500, maxSalary: 6000 },
  { jobTitleId: 8, jobTitle: 'Developer', jobLevel: 5, minSalary: 2500, maxSalary: 4500 },
  { jobTitleId: 9, jobTitle: 'Junior Developer', jobLevel: 4, minSalary: 1800, maxSalary: 3000 },
  { jobTitleId: 10, jobTitle: 'Business Analyst', jobLevel: 5, minSalary: 2500, maxSalary: 4500 },
  { jobTitleId: 11, jobTitle: 'Sales Manager', jobLevel: 8, minSalary: 5000, maxSalary: 9000 },
  { jobTitleId: 12, jobTitle: 'Sales Representative', jobLevel: 5, minSalary: 2000, maxSalary: 4000 },
  { jobTitleId: 13, jobTitle: 'Customer Service Rep', jobLevel: 4, minSalary: 1500, maxSalary: 2500 },
  { jobTitleId: 14, jobTitle: 'Accountant', jobLevel: 5, minSalary: 2500, maxSalary: 4000 },
  { jobTitleId: 15, jobTitle: 'HR Manager', jobLevel: 8, minSalary: 4500, maxSalary: 8000 },
  { jobTitleId: 16, jobTitle: 'HR Specialist', jobLevel: 5, minSalary: 2200, maxSalary: 3500 },
  { jobTitleId: 17, jobTitle: 'Warehouse Supervisor', jobLevel: 6, minSalary: 2800, maxSalary: 4500 },
  { jobTitleId: 18, jobTitle: 'Warehouse Clerk', jobLevel: 4, minSalary: 1500, maxSalary: 2200 },
];

const firstNames = [
  'Karim', 'Leila', 'Ahmed', 'Sarra', 'Nadia', 'Youssef', 'Amine', 'Mehdi', 'Salma', 'Fares',
  'Mohamed', 'Tarek', 'Amira', 'Hela', 'Sami', 'Rania', 'Omar', 'Ines', 'Khaled', 'Marwa',
  'Hassen', 'Jihen', 'Ali', 'Cyrine', 'Sofiene', 'Manel', 'Wael', 'Dorsaf', 'Anis', 'Rym',
  'Bilel', 'Awatef', 'Chokri', 'Emna', 'Fathi', 'Ghada', 'Hichem', 'Imen', 'Jalel', 'Kawther',
];

const lastNames = [
  'Bahri', 'Mansour', 'Hajji', 'Bouslama', 'Khemiri', 'Riahi', 'Cherif', 'Bouazizi', 'Laabidi', 'Dridi',
  'Jebali', 'Zouari', 'Oueslati', 'Sassi', 'Guesmi', 'Ben Ali', 'Haddad', 'Mokni', 'Trabelsi', 'Sfaxi',
  'Karray', 'Ben Salem', 'Rhaiem', 'Chaabani', 'Ferchichi', 'Gharbi', 'Lajimi', 'Mechergui', 'Nefzi', 'Ouni',
];

/* ---------- Génération des employés ---------- */

const MANAGEMENT_JOB_LEVEL = 7; // jobLevel >= 7 => encadrant (CEO .. Team Lead)

const employees = [];
let nextId = 1;

// 1) Les managers (un par département, quelques-uns en plus)
const managerPool = [];
for (const dept of departments) {
  const mgmtJobs = jobs.filter((j) => j.jobLevel >= MANAGEMENT_JOB_LEVEL);
  const job = pick(mgmtJobs);
  const firstName = pick(firstNames);
  const lastName = pick(lastNames);
  const emp = {
    employeeId: nextId++,
    companyId: dept.companyId,
    firstName,
    lastName,
    email: `${firstName.toLowerCase()}.${lastName.toLowerCase()}@example.tn`,
    departmentId: dept.departmentId,
    jobTitleId: job.jobTitleId,
    managerEmployeeId: null,
    hireDate: '2015-03-01',
    gender: rand() < 0.5 ? 'M' : 'F',
    employmentType: 'Full-Time',
    employmentStatus: 'Active',
    baseSalary: round50(ri(job.minSalary, job.maxSalary)),
    currency: 'TND',
    isActive: true,
  };
  employees.push(emp);
  managerPool.push(emp);
}

// 2) Les employés classiques
const staffJobs = jobs.filter((j) => j.jobLevel < MANAGEMENT_JOB_LEVEL);
for (let i = 0; i < 132; i += 1) {
  const dept = pick(departments);
  const job = pick(staffJobs);
  const firstName = pick(firstNames);
  const lastName = pick(lastNames);
  const deptManagers = managerPool.filter((m) => m.departmentId === dept.departmentId);
  const manager = deptManagers.length ? pick(deptManagers) : pick(managerPool);
  employees.push({
    employeeId: nextId++,
    companyId: dept.companyId,
    firstName,
    lastName,
    email: `${firstName.toLowerCase()}.${lastName.toLowerCase()}@example.tn`,
    departmentId: dept.departmentId,
    jobTitleId: job.jobTitleId,
    managerEmployeeId: manager.employeeId,
    hireDate: `20${ri(16, 23)}-${String(ri(1, 12)).padStart(2, '0')}-${String(ri(1, 28)).padStart(2, '0')}`,
    gender: rand() < 0.56 ? 'M' : 'F',
    employmentType: rand() < 0.9 ? 'Full-Time' : 'Part-Time',
    employmentStatus: rand() < 0.93 ? 'Active' : 'Inactive',
    baseSalary: round50(ri(job.minSalary, job.maxSalary)),
    currency: 'TND',
    isActive: rand() < 0.95,
  });
}

const fullName = (e) => `${e.firstName} ${e.lastName}`;

/* ---------- Filtrage (simulation des paramètres GET) ---------- */

const matchId = (value, filter) =>
  filter === undefined || filter === null || filter === '' || Number(value) === Number(filter);

const matchStr = (value, filter) =>
  filter === undefined || filter === null || filter === '' || String(value) === String(filter);

function applyFilters(list, params) {
  return list.filter((e) =>
    matchId(e.companyId, params.companyId) &&
    matchId(e.departmentId, params.departmentId) &&
    matchId(e.jobTitleId, params.jobTitleId) &&
    matchId(e.managerEmployeeId, params.managerId) &&
    matchStr(e.employmentType, params.employmentType) &&
    matchStr(e.employmentStatus, params.employmentStatus) &&
    matchStr(e.gender, params.gender),
  );
}

/* ---------- Agrégations ---------- */

function aggregate(list, keyFn) {
  const map = new Map();
  for (const e of list) {
    const key = keyFn(e);
    if (!map.has(key)) map.set(key, { count: 0, salarySum: 0 });
    const agg = map.get(key);
    agg.count += 1;
    agg.salarySum += e.baseSalary;
  }
  return map;
}

function buildKpi(list) {
  const totalEmployees = list.length;
  const totalSalaryMass = list.reduce((s, e) => s + e.baseSalary, 0);
  return {
    totalEmployees,
    averageSalary: totalEmployees ? totalSalaryMass / totalEmployees : 0,
    maxSalary: totalEmployees ? Math.max(...list.map((e) => e.baseSalary)) : null,
    minSalary: totalEmployees ? Math.min(...list.map((e) => e.baseSalary)) : null,
    totalSalaryMass,
  };
}

function buildPageOne(params) {
  const list = applyFilters(employees, params);
  const byDept = aggregate(list, (e) => e.departmentId);
  const deptRows = departments
    .filter((d) => byDept.has(d.departmentId))
    .map((d) => ({ name: d.departmentName, count: byDept.get(d.departmentId).count }));
  const avgRows = departments
    .filter((d) => byDept.has(d.departmentId))
    .map((d) => {
      const a = byDept.get(d.departmentId);
      return { name: d.departmentName, value: a.count ? a.salarySum / a.count : 0 };
    });

  const genderMap = new Map();
  for (const e of list) {
    const key = `${e.departmentId}|${e.gender}`;
    genderMap.set(key, (genderMap.get(key) || 0) + 1);
  }
  const genderRows = [];
  for (const d of departments) {
    for (const gender of ['F', 'M']) {
      const count = genderMap.get(`${d.departmentId}|${gender}`);
      if (count) genderRows.push({ department: d.departmentName, gender, count });
    }
  }

  return {
    employeesByDepartment: deptRows,
    averageSalaryByDepartment: avgRows,
    genderDistribution: genderRows,
    kpi: buildKpi(list),
  };
}

function buildPageTwo(params) {
  const list = applyFilters(employees, params);
  const byJob = aggregate(list, (e) => e.jobTitleId);
  const jobRows = jobs
    .filter((j) => byJob.has(j.jobTitleId))
    .map((j) => ({ name: j.jobTitle, count: byJob.get(j.jobTitleId).count }));
  const avgRows = jobs
    .filter((j) => byJob.has(j.jobTitleId))
    .map((j) => {
      const a = byJob.get(j.jobTitleId);
      return { name: j.jobTitle, value: a.count ? a.salarySum / a.count : 0 };
    });

  const typeMap = aggregate(list, (e) => e.employmentType);
  const typeRows = [...typeMap.entries()].map(([employmentType, a]) => ({ employmentType, count: a.count }));

  return {
    employeesByJobTitle: jobRows,
    averageSalaryByJobTitle: avgRows,
    employmentTypeDistribution: typeRows,
    kpi: buildKpi(list),
  };
}

function buildPageThree(params) {
  const list = applyFilters(employees, params);
  const byCompany = aggregate(list, (e) => e.companyId);
  const companyRows = companies
    .filter((c) => byCompany.has(c.companyId))
    .map((c) => {
      const a = byCompany.get(c.companyId);
      return {
        companyName: c.companyName,
        employeeCount: a.count,
        averageSalary: a.count ? a.salarySum / a.count : 0,
        totalSalaryMass: a.salarySum,
      };
    });

  return {
    companiesData: companyRows,
    employeesByCompany: companyRows.map((c) => ({ name: c.companyName, count: c.employeeCount })),
    averageSalaryByCompany: companyRows.map((c) => ({ name: c.companyName, value: c.averageSalary })),
  };
}

function buildPageFour(params) {
  const list = applyFilters(employees, params).filter((e) => e.managerEmployeeId != null);
  const byManager = aggregate(list, (e) => e.managerEmployeeId);

  const managerRows = [...byManager.entries()]
    .map(([managerId, a]) => {
      const mgr = employees.find((e) => e.employeeId === managerId);
      return {
        managerId,
        managerName: mgr ? fullName(mgr) : `Manager ${managerId}`,
        subordinateCount: a.count,
        averageSalary: a.count ? a.salarySum / a.count : 0,
      };
    })
    .sort((a, b) => b.subordinateCount - a.subordinateCount);

  return {
    managers: managerRows,
    employeesByManager: managerRows.map((m) => ({ name: `Manager ${m.managerId}`, count: m.subordinateCount })),
    averageSalaryByManager: managerRows.map((m) => ({ name: `Manager ${m.managerId}`, value: m.averageSalary })),
    departmentStats: { totalDepartments: new Set(list.map((e) => e.departmentId)).size },
  };
}

/* ---------- Formes "public" (comme le vrai backend) ---------- */

const publicEmployee = (e) => ({
  employeeId: e.employeeId,
  companyId: e.companyId,
  firstName: e.firstName,
  lastName: e.lastName,
  email: e.email,
  departmentId: e.departmentId,
  jobTitleId: e.jobTitleId,
  baseSalary: e.baseSalary,
  isActive: e.isActive,
});

const routes = {
  '/api/dashboard/companies': () => companies.map(({ ...c }) => c),
  '/api/dashboard/departements': () => departments.map(({ ...d }) => d),
  '/api/dashboard/jobs': () =>
    jobs.map(({ jobTitleId, jobTitle, jobLevel, minSalary, maxSalary }) => ({
      jobTitleId, jobTitle, jobLevel, minSalary, maxSalary,
    })),
  '/api/dashboard/employees': () => employees.map(publicEmployee),
  '/api/dashboard/page1': (params) => buildPageOne(params),
  '/api/dashboard/page2': (params) => buildPageTwo(params),
  '/api/dashboard/page3': (params) => buildPageThree(params),
  '/api/dashboard/page4': (params) => buildPageFour(params),
};

export async function mockHandler(path, { params, signal } = {}) {
  const latency = ri(180, 420);
  await new Promise((resolve, reject) => {
    const timer = setTimeout(resolve, latency);
    if (signal) {
      signal.addEventListener('abort', () => {
        clearTimeout(timer);
        const err = new Error('Aborted');
        err.name = 'AbortError';
        reject(err);
      }, { once: true });
    }
  });

  const handler = routes[path];
  if (!handler) {
    throw new Error(`Route mock introuvable : ${path}`);
  }

  console.info(`[Mock] GET ${path}${params && Object.keys(params).length ? ` ${JSON.stringify(params)}` : ''} (latence simulée ${latency} ms)`);
  return handler(params || {});
}

/* ---------- Assistant (chat RAG simulé) ---------- */

function chatKpiChunk() {
  const active = employees.filter((e) => e.isActive && e.employmentStatus === 'Active');
  const kpi = buildKpi(active);
  return {
    chunk: `KPIs globaux : ${kpi.totalEmployees} employés actifs au total ; salaire moyen ${kpi.averageSalary.toFixed(2)} TND ; min ${kpi.minSalary} ; max ${kpi.maxSalary} ; masse salariale ${kpi.totalSalaryMass} TND`,
    kpi,
  };
}

function buildChatResponse(body) {
  const message = String(body?.message || '').toLowerCase();
  const { kpi } = chatKpiChunk();
  const sources = [
    { type: 'chunk', label: 'KPIs globaux (mock)', detail: `totalEmployees=${kpi.totalEmployees} ; averageSalary=${kpi.averageSalary.toFixed(2)}` },
  ];

  const fmt = (n) => Number(n).toLocaleString('fr-FR', { maximumFractionDigits: 2 });

  // --- nombre total d'employés ---
  if (/combien[^\n]*employ|nombre[^\n]*employ|total[^\n]*employ|employ[^\n]*au total/.test(message)) {
    return {
      answer: `L'entrepôt contient **${kpi.totalEmployees} employés actifs** au total (données fictives du mode mock).\n\nMasse salariale totale : **${fmt(kpi.totalSalaryMass)} TND** · salaire moyen : **${fmt(kpi.averageSalary)} TND**.\n\n*Source : chunk « KPIs globaux » de l'index RAG.*`,
      sources,
      llmFallback: false,
      llmError: null,
    };
  }

  // --- salaire moyen par département ---
  if (/salaire moyen[^\n]*(par|département|department)|moyenne[^\n]*département/.test(message)) {
    const byDept = new Map();
    for (const e of employees) {
      if (!(e.isActive && e.employmentStatus === 'Active')) continue;
      if (!byDept.has(e.departmentId)) byDept.set(e.departmentId, { count: 0, sum: 0 });
      const agg = byDept.get(e.departmentId);
      agg.count += 1;
      agg.sum += e.baseSalary;
    }
    const rows = departments
      .filter((d) => byDept.has(d.departmentId))
      .map((d) => {
        const agg = byDept.get(d.departmentId);
        return `| ${d.departmentName} | ${agg.count} | ${fmt(agg.sum / agg.count)} |`;
      })
      .slice(0, 8);
    sources.push({ type: 'computation', label: 'Calcul : avg baseSalary par department (mock)', detail: `${rows.length} lignes` });
    return {
      answer: `Salaire moyen par département (mode mock) :\n\n| Département | Employés | Salaire moyen (TND) |\n|---|---|---|\n${rows.join('\n')}`,
      sources,
      llmFallback: false,
      llmError: null,
    };
  }

  // --- femmes dans un département ---
  if (/femme|female|genre/.test(message)) {
    const fCount = employees.filter((e) => e.isActive && e.employmentStatus === 'Active' && e.gender === 'F').length;
    const mCount = employees.filter((e) => e.isActive && e.employmentStatus === 'Active' && e.gender === 'M').length;
    sources.push({ type: 'computation', label: 'Calcul : count employees par gender (mock)', detail: `F=${fCount}, M=${mCount}` });
    return {
      answer: `Répartition par genre (mode mock) : **${fCount} femmes** et **${mCount} hommes** parmi les ${kpi.totalEmployees} employés actifs.`,
      sources,
      llmFallback: false,
      llmError: null,
    };
  }

  // --- managers avec le plus de subordonnés ---
  if (/manager|subordon|manageur/.test(message)) {
    const withMgr = employees.filter((e) => e.managerEmployeeId != null);
    const byMgr = new Map();
    for (const e of withMgr) byMgr.set(e.managerEmployeeId, (byMgr.get(e.managerEmployeeId) || 0) + 1);
    const top = [...byMgr.entries()]
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([mgrId, count]) => {
        const mgr = employees.find((e) => e.employeeId === mgrId);
        return `| ${mgr ? `${mgr.firstName} ${mgr.lastName}` : `Manager ${mgrId}`} | ${count} |`;
      });
    sources.push({ type: 'computation', label: 'Calcul : count employees par manager (mock)', detail: `${top.length} lignes` });
    return {
      answer: `Managers avec le plus de subordonnés (mode mock) :\n\n| Manager | Subordonnés |\n|---|---|\n${top.join('\n')}`,
      sources,
      llmFallback: false,
      llmError: null,
    };
  }

  // --- hors périmètre ---
  return {
    answer: `Je ne trouve pas cette information dans les données de l'entrepôt RH (mode mock). Je peux répondre à des questions sur les effectifs, les salaires, les départements, les postes, les sociétés et les managers.`,
    sources,
    llmFallback: false,
    llmError: null,
  };
}

const postRoutes = {
  '/api/chat': (body) => buildChatResponse(body),
  '/api/chat/status': () => ({ provider: 'mock', model: 'mock-llm:free', keyConfigured: true }),
  '/api/chat/reindex': () => ({ success: true, message: 'Index reconstruit (mode mock)' }),
};

export async function mockPostHandler(path, { body, signal } = {}) {
  const latency = ri(180, 420);
  await new Promise((resolve, reject) => {
    const timer = setTimeout(resolve, latency);
    if (signal) {
      signal.addEventListener('abort', () => {
        clearTimeout(timer);
        const err = new Error('Aborted');
        err.name = 'AbortError';
        reject(err);
      }, { once: true });
    }
  });

  const handler = postRoutes[path];
  if (!handler) {
    throw new Error(`Route mock POST introuvable : ${path}`);
  }

  console.info(`[Mock] POST ${path} (latence simulée ${latency} ms)`);
  return handler(body || {});
}
