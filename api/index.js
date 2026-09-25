const express = require('express');
const cors = require('cors');
const pool = require('./db');

const app = express();

app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());

// ─── Schema Setup Utility ──────────────────────────────────────────────────────
async function ensureSchemaCreated(conn) {
  await conn.query(`
    CREATE TABLE IF NOT EXISTS countries (
      country_code CHAR(3) PRIMARY KEY,
      country_name VARCHAR(255) NOT NULL,
      currency_code CHAR(3) NOT NULL DEFAULT 'USD'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  `);

  await conn.query(`
    CREATE TABLE IF NOT EXISTS departments (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      code VARCHAR(255) NOT NULL UNIQUE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  `);

  await conn.query(`
    CREATE TABLE IF NOT EXISTS employees (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      first_name VARCHAR(255) NOT NULL,
      last_name VARCHAR(255) NOT NULL,
      email VARCHAR(255) NOT NULL UNIQUE,
      job_title VARCHAR(255) NOT NULL DEFAULT 'Employee',
      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
      country_code CHAR(3) NOT NULL DEFAULT 'USA',
      department_id BIGINT NOT NULL DEFAULT 1,
      hire_date DATE DEFAULT '2023-01-01',
      INDEX idx_country (country_code),
      INDEX idx_dept (department_id),
      INDEX idx_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  `);

  await conn.query(`
    CREATE TABLE IF NOT EXISTS compensation (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      employee_id BIGINT NOT NULL UNIQUE,
      base_pay DECIMAL(12,2) NOT NULL DEFAULT 0.00,
      pf_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00,
      other_deductions DECIMAL(12,2) NOT NULL DEFAULT 0.00,
      paid_leaves_allowance INT NOT NULL DEFAULT 20,
      sick_leaves_allowance INT NOT NULL DEFAULT 10,
      INDEX idx_employee (employee_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  `);

  // Ensure department table column alias migration
  try {
    const [cols] = await conn.query('SHOW COLUMNS FROM departments');
    const colNames = cols.map(c => c.Field);
    if (!colNames.includes('name') && colNames.includes('department_name')) {
      await conn.query('ALTER TABLE departments CHANGE COLUMN department_name name VARCHAR(255) NOT NULL');
    }
    if (!colNames.includes('code')) {
      await conn.query("ALTER TABLE departments ADD COLUMN code VARCHAR(255) NOT NULL DEFAULT 'DEPT'");
    }
  } catch (e) {
    console.error('Migration warning:', e.message);
  }
}

// ─── Health ───────────────────────────────────────────────────────────────────
app.get('/api/health', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query('SELECT COUNT(*) AS count FROM employees');
    conn.release();
    res.json({ status: 'ok', database: 'connected', totalEmployees: rows[0].count });
  } catch (err) {
    res.status(500).json({ status: 'error', message: err.message });
  }
});

// Root
app.get('/', (req, res) => {
  res.json({ message: 'ACME Salary Management API', version: '1.0.0', status: 'running' });
});

// ─── Database Seeding Endpoint ─────────────────────────────────────────────────
app.post('/api/v1/seed', async (req, res) => {
  const conn = await pool.getConnection();
  try {
    await ensureSchemaCreated(conn);

    // 1. Seed Countries if empty
    const [existingCountries] = await conn.query('SELECT COUNT(*) as count FROM countries');
    if (existingCountries[0].count === 0) {
      const countriesData = [
        ['USA', 'United States', 'USD'],
        ['IND', 'India', 'INR'],
        ['GBR', 'United Kingdom', 'GBP'],
        ['JPN', 'Japan', 'JPY'],
        ['DEU', 'Germany', 'EUR'],
        ['CAN', 'Canada', 'CAD'],
        ['AUS', 'Australia', 'AUD'],
        ['BRA', 'Brazil', 'BRL'],
        ['FRA', 'France', 'EUR'],
        ['SGP', 'Singapore', 'SGD'],
        ['ESP', 'Spain', 'EUR'],
        ['ITA', 'Italy', 'EUR'],
        ['NLD', 'Netherlands', 'EUR'],
        ['SWE', 'Sweden', 'SEK'],
        ['CHE', 'Switzerland', 'CHF'],
        ['MEX', 'Mexico', 'MXN'],
        ['KOR', 'South Korea', 'KRW']
      ];
      await conn.query('INSERT IGNORE INTO countries (country_code, country_name, currency_code) VALUES ?', [countriesData]);
    }

    // 2. Seed Departments if empty
    const [existingDepts] = await conn.query('SELECT COUNT(*) as count FROM departments');
    if (existingDepts[0].count === 0) {
      const depts = [
        ['Engineering', 'ENG'],
        ['Human Resources', 'HR'],
        ['Sales', 'SALES'],
        ['Marketing', 'MKTG'],
        ['Finance', 'FIN'],
        ['Product Management', 'PROD'],
        ['Design', 'DESIGN'],
        ['Legal', 'LEGAL'],
        ['Operations', 'OPS'],
        ['Quality Assurance', 'QA']
      ];
      await conn.query('INSERT IGNORE INTO departments (name, code) VALUES ?', [depts]);
    }

    // Retrieve country codes and department IDs
    const [countryRows] = await conn.query('SELECT country_code FROM countries');
    const countryCodes = countryRows.map(r => r.country_code);
    const [deptRows] = await conn.query('SELECT id FROM departments');
    const deptIds = deptRows.map(r => r.id);

    // 3. Check current employees count
    const [empCountRow] = await conn.query('SELECT COUNT(*) as count FROM employees');
    let currentEmpCount = empCountRow[0].count;

    const targetCount = 10000;
    if (currentEmpCount >= targetCount) {
      conn.release();
      return res.json({
        status: 'success',
        message: `Database already seeded with ${currentEmpCount} employees`,
        totalEmployees: currentEmpCount
      });
    }

    const toCreate = targetCount - currentEmpCount;
    const firstNames = ['James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda', 'William', 'Elizabeth', 'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica', 'Thomas', 'Sarah', 'Charles', 'Karen', 'Rahul', 'Priya', 'Amit', 'Neha', 'Sanjay', 'Ananya', 'Vikram', 'Pooja', 'Alex', 'Elena', 'Hans', 'Yuki', 'Carlos', 'Fatima', 'Jean', 'Sophie'];
    const lastNames = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Sharma', 'Patel', 'Verma', 'Kumar', 'Singh', 'Gupta', 'Muller', 'Sato', 'Tanaka', 'Silva', 'Dubois'];
    const jobTitles = ['Software Engineer', 'Senior Engineer', 'Engineering Manager', 'HR Specialist', 'Sales Representative', 'Account Executive', 'Marketing Coordinator', 'Financial Analyst', 'Product Manager', 'UX Designer', 'Legal Counsel', 'Operations Lead', 'QA Specialist'];

    const batchSize = 500;
    let createdCount = 0;

    for (let batchStart = 0; batchStart < toCreate; batchStart += batchSize) {
      const currentBatch = Math.min(batchSize, toCreate - batchStart);
      const empValues = [];

      for (let i = 0; i < currentBatch; i++) {
        const idx = currentEmpCount + createdCount + i + 1;
        const fn = firstNames[Math.floor(Math.random() * firstNames.length)];
        const ln = lastNames[Math.floor(Math.random() * lastNames.length)];
        const email = `${fn.toLowerCase()}.${ln.toLowerCase()}.${idx}@acme-corp.com`;
        const title = jobTitles[Math.floor(Math.random() * jobTitles.length)];
        const status = (idx % 100 === 0) ? 'TERMINATED' : 'ACTIVE';
        const cc = countryCodes[Math.floor(Math.random() * countryCodes.length)];
        const deptId = deptIds[Math.floor(Math.random() * deptIds.length)];
        const hireDate = new Date(2018 + Math.floor(Math.random() * 6), Math.floor(Math.random() * 12), 1 + Math.floor(Math.random() * 28)).toISOString().split('T')[0];

        empValues.push([fn, ln, email, title, status, cc, deptId, hireDate]);
      }

      const [empResult] = await conn.query(
        'INSERT INTO employees (first_name, last_name, email, job_title, status, country_code, department_id, hire_date) VALUES ?',
        [empValues]
      );

      const firstInsertedId = empResult.insertId;
      const compValues = [];
      for (let i = 0; i < currentBatch; i++) {
        const empId = firstInsertedId + i;
        const basePay = Math.floor(45000 + Math.random() * 110000);
        const pf = Math.floor(basePay * 0.05);
        const other = Math.floor(basePay * 0.02);
        compValues.push([empId, basePay, pf, other, 20, 10]);
      }

      await conn.query(
        'INSERT INTO compensation (employee_id, base_pay, pf_deduction, other_deductions, paid_leaves_allowance, sick_leaves_allowance) VALUES ?',
        [compValues]
      );

      createdCount += currentBatch;
    }

    const [finalCountRow] = await conn.query('SELECT COUNT(*) as count FROM employees');
    conn.release();

    res.json({
      status: 'success',
      message: `Database successfully seeded with ${finalCountRow[0].count} total employees`,
      totalEmployees: finalCountRow[0].count
    });

  } catch (err) {
    conn.release();
    console.error('Seeding error:', err);
    res.status(500).json({ error: err.message });
  }
});

// ─── Analytics Summary ────────────────────────────────────────────────────────
app.get('/api/v1/analytics/summary', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query(`
      SELECT
        COALESCE(SUM(c.base_pay + c.pf_deduction + c.other_deductions), 0) AS totalCompanyCost,
        COALESCE(AVG(c.base_pay), 0) AS globalAverageSalary,
        COUNT(e.id) AS activeHeadcount
      FROM employees e
      LEFT JOIN compensation c ON e.id = c.employee_id
      WHERE UPPER(e.status) = 'ACTIVE'
    `);
    conn.release();
    const s = rows[0] || {};
    res.json({
      totalCompanyCost: parseFloat(s.totalCompanyCost) || 0,
      globalAverageSalary: parseFloat(s.globalAverageSalary) || 0,
      medianSalary: parseFloat(s.globalAverageSalary) || 0,
      activeHeadcount: parseInt(s.activeHeadcount, 10) || 0
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Country Breakdown ────────────────────────────────────────────────────────
app.get('/api/v1/analytics/breakdown/country', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query(`
      SELECT
        e.country_code AS countryCode,
        c_name.country_name AS countryName,
        COUNT(e.id) AS headcount,
        COALESCE(AVG(c.base_pay), 0) AS averageBaseSalary,
        COALESCE(SUM(c.base_pay + c.pf_deduction + c.other_deductions), 0) AS totalCtcSpend
      FROM employees e
      JOIN countries c_name ON e.country_code = c_name.country_code
      LEFT JOIN compensation c ON e.id = c.employee_id
      WHERE UPPER(e.status) = 'ACTIVE'
      GROUP BY e.country_code, c_name.country_name
      ORDER BY totalCtcSpend DESC
    `);
    conn.release();
    res.json(rows.map(r => ({
      countryCode: r.countryCode,
      countryName: r.countryName,
      headcount: parseInt(r.headcount, 10),
      averageBaseSalary: parseFloat(r.averageBaseSalary),
      totalCtcSpend: parseFloat(r.totalCtcSpend)
    })));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Department Breakdown ─────────────────────────────────────────────────────
app.get('/api/v1/analytics/breakdown/department', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query(`
      SELECT
        d.id AS departmentId,
        d.name AS departmentName,
        COUNT(e.id) AS headcount,
        COALESCE(AVG(c.base_pay), 0) AS averageBaseSalary,
        COALESCE(SUM(c.base_pay + c.pf_deduction + c.other_deductions), 0) AS totalCtcSpend
      FROM departments d
      JOIN employees e ON e.department_id = d.id
      LEFT JOIN compensation c ON e.id = c.employee_id
      WHERE UPPER(e.status) = 'ACTIVE'
      GROUP BY d.id, d.name
      ORDER BY headcount DESC
    `);
    conn.release();
    res.json(rows.map(r => ({
      departmentId: parseInt(r.departmentId, 10),
      departmentName: r.departmentName,
      headcount: parseInt(r.headcount, 10),
      averageBaseSalary: parseFloat(r.averageBaseSalary),
      totalCtcSpend: parseFloat(r.totalCtcSpend)
    })));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Top Earners ──────────────────────────────────────────────────────────────
app.get('/api/v1/analytics/top-earners', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const limit = parseInt(req.query.limit, 10) || 10;
    const countryCode = req.query.countryCode;
    let query = `
      SELECT e.id, e.first_name AS firstName, e.last_name AS lastName,
             e.job_title AS jobTitle, d.name AS department,
             e.country_code AS countryCode, c.base_pay AS basePay
      FROM employees e
      JOIN departments d ON e.department_id = d.id
      JOIN compensation c ON e.id = c.employee_id
      WHERE UPPER(e.status) = 'ACTIVE'
    `;
    const params = [];
    if (countryCode) {
      query += ' AND e.country_code = ?';
      params.push(countryCode);
    }
    query += ` ORDER BY c.base_pay DESC LIMIT ?`;
    params.push(limit);
    const [rows] = await conn.query(query, params);
    conn.release();
    res.json(rows.map(r => ({
      id: r.id,
      firstName: r.firstName,
      lastName: r.lastName,
      jobTitle: r.jobTitle,
      department: r.department,
      countryCode: r.countryCode,
      basePay: parseFloat(r.basePay)
    })));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Employees List ───────────────────────────────────────────────────────────
app.get('/api/v1/employees', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const page = parseInt(req.query.page, 10) || 0;
    const size = parseInt(req.query.size, 10) || 25;
    const offset = page * size;
    const { departmentId, countryCode, status, search } = req.query;

    let where = 'WHERE 1=1';
    const params = [];

    if (departmentId) { where += ' AND e.department_id = ?'; params.push(departmentId); }
    if (countryCode) { where += ' AND e.country_code = ?'; params.push(countryCode); }
    if (status) { where += ' AND UPPER(e.status) = UPPER(?)'; params.push(status); }
    if (search) {
      where += ' AND (e.first_name LIKE ? OR e.last_name LIKE ? OR e.email LIKE ? OR e.job_title LIKE ?)';
      const s = `%${search}%`;
      params.push(s, s, s, s);
    }

    const countQuery = `SELECT COUNT(*) AS total FROM employees e ${where}`;
    const [countRows] = await conn.query(countQuery, params);
    const total = parseInt(countRows[0].total, 10);

    const dataQuery = `
      SELECT e.id, e.first_name AS firstName, e.last_name AS lastName,
             e.email, e.job_title AS jobTitle, e.status,
             e.country_code AS countryCode, e.hire_date AS hireDate,
             d.name AS department, d.id AS departmentId,
             c.base_pay AS basePay, c.pf_deduction AS pfDeduction,
             c.other_deductions AS otherDeductions
      FROM employees e
      LEFT JOIN departments d ON e.department_id = d.id
      LEFT JOIN compensation c ON e.id = c.employee_id
      ${where}
      ORDER BY e.last_name, e.first_name
      LIMIT ? OFFSET ?
    `;
    const [rows] = await conn.query(dataQuery, [...params, size, offset]);
    conn.release();

    res.json({
      content: rows.map(r => ({
        id: r.id,
        firstName: r.firstName,
        lastName: r.lastName,
        email: r.email,
        jobTitle: r.jobTitle,
        status: r.status,
        countryCode: r.countryCode,
        hireDate: r.hireDate,
        department: { id: r.departmentId, departmentName: r.department },
        compensation: r.basePay != null ? {
          basePay: parseFloat(r.basePay),
          pfDeduction: parseFloat(r.pfDeduction),
          otherDeductions: parseFloat(r.otherDeductions)
        } : null
      })),
      totalElements: total,
      totalPages: Math.ceil(total / size),
      number: page,
      size: size
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Get Employee by ID ───────────────────────────────────────────────────────
app.get('/api/v1/employees/:id', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query(`
      SELECT e.id, e.first_name AS firstName, e.last_name AS lastName,
             e.email, e.job_title AS jobTitle, e.status,
             e.country_code AS countryCode, e.hire_date AS hireDate,
             d.name AS department, d.id AS departmentId,
             c.base_pay AS basePay, c.pf_deduction AS pfDeduction,
             c.other_deductions AS otherDeductions
      FROM employees e
      LEFT JOIN departments d ON e.department_id = d.id
      LEFT JOIN compensation c ON e.id = c.employee_id
      WHERE e.id = ?
    `, [req.params.id]);
    conn.release();
    if (!rows.length) return res.status(404).json({ error: 'Employee not found' });
    const r = rows[0];
    res.json({
      id: r.id, firstName: r.firstName, lastName: r.lastName,
      email: r.email, jobTitle: r.jobTitle, status: r.status,
      countryCode: r.countryCode, hireDate: r.hireDate,
      department: { id: r.departmentId, departmentName: r.department },
      compensation: r.basePay != null ? {
        basePay: parseFloat(r.basePay),
        pfDeduction: parseFloat(r.pfDeduction),
        otherDeductions: parseFloat(r.otherDeductions)
      } : null
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Create Employee ──────────────────────────────────────────────────────────
app.post('/api/v1/employees', async (req, res) => {
  const conn = await pool.getConnection();
  try {
    await ensureSchemaCreated(conn);
    await conn.beginTransaction();
    const { firstName, lastName, email, jobTitle, status, countryCode, departmentId, hireDate, basePay, pfDeduction, otherDeductions } = req.body;
    const [result] = await conn.query(
      'INSERT INTO employees (first_name, last_name, email, job_title, status, country_code, department_id, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
      [firstName, lastName, email, jobTitle, status || 'ACTIVE', countryCode, departmentId, hireDate]
    );
    const empId = result.insertId;
    if (basePay != null) {
      await conn.query(
        'INSERT INTO compensation (employee_id, base_pay, pf_deduction, other_deductions, paid_leaves_allowance, sick_leaves_allowance) VALUES (?, ?, ?, ?, 20, 10)',
        [empId, basePay, pfDeduction || 0, otherDeductions || 0]
      );
    }
    await conn.commit();
    conn.release();
    res.status(201).json({ id: empId, message: 'Employee created' });
  } catch (err) {
    await conn.rollback();
    conn.release();
    res.status(500).json({ error: err.message });
  }
});

// ─── Update Employee Salary ───────────────────────────────────────────────────
app.put('/api/v1/employees/:id/salary', async (req, res) => {
  const conn = await pool.getConnection();
  try {
    await ensureSchemaCreated(conn);
    await conn.beginTransaction();
    const { basePay, pfDeduction, otherDeductions, status } = req.body;
    const empId = req.params.id;
    if (status) {
      await conn.query('UPDATE employees SET status = ? WHERE id = ?', [status, empId]);
    }
    const [existing] = await conn.query('SELECT id FROM compensation WHERE employee_id = ?', [empId]);
    if (existing.length) {
      await conn.query(
        'UPDATE compensation SET base_pay = ?, pf_deduction = ?, other_deductions = ? WHERE employee_id = ?',
        [basePay, pfDeduction || 0, otherDeductions || 0, empId]
      );
    } else {
      await conn.query(
        'INSERT INTO compensation (employee_id, base_pay, pf_deduction, other_deductions, paid_leaves_allowance, sick_leaves_allowance) VALUES (?, ?, ?, ?, 20, 10)',
        [empId, basePay, pfDeduction || 0, otherDeductions || 0]
      );
    }
    await conn.commit();
    conn.release();
    res.json({ message: 'Salary updated successfully' });
  } catch (err) {
    await conn.rollback();
    conn.release();
    res.status(500).json({ error: err.message });
  }
});

// ─── Departments ──────────────────────────────────────────────────────────────
app.get('/api/v1/departments', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query('SELECT id, name AS departmentName FROM departments ORDER BY name');
    conn.release();
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─── Countries ────────────────────────────────────────────────────────────────
app.get('/api/v1/countries', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await ensureSchemaCreated(conn);
    const [rows] = await conn.query('SELECT country_code AS countryCode, country_name AS countryName FROM countries ORDER BY country_name');
    conn.release();
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Export for Vercel serverless
module.exports = app;

if (process.env.NODE_ENV !== 'production' && !process.env.VERCEL) {
  const PORT = process.env.PORT || 8080;
  app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
  });
}
