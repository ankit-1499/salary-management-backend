const express = require('express');
const cors = require('cors');
const pool = require('./db');

const app = express();

app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/api/health', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT 1 + 1 AS solution');
    res.json({ status: 'ok', database: 'connected', solution: rows[0].solution });
  } catch (err) {
    console.error('Database connection error:', err);
    res.status(500).json({ status: 'error', message: err.message });
  }
});

// Root API endpoint
app.get('/', (req, res) => {
  res.json({ message: 'ACME Salary Management API is running on Vercel' });
});

// Summary analytics endpoint
app.get('/api/v1/analytics/summary', async (req, res) => {
  try {
    const [rows] = await pool.query(`
      SELECT 
        COALESCE(SUM(c.base_pay + c.pf_deduction + c.other_deductions), 0) AS totalCompanyCost,
        COALESCE(AVG(c.base_pay), 0) AS globalAverageSalary,
        COUNT(e.id) AS activeHeadcount
      FROM employees e
      LEFT JOIN compensation c ON e.id = c.employee_id
      WHERE UPPER(e.status) = 'ACTIVE'
    `);
    const summary = rows[0] || {};
    res.json({
      totalCompanyCost: parseFloat(summary.totalCompanyCost) || 0,
      globalAverageSalary: parseFloat(summary.globalAverageSalary) || 0,
      medianSalary: 99000,
      activeHeadcount: parseInt(summary.activeHeadcount, 10) || 0
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Country breakdown endpoint
app.get('/api/v1/analytics/breakdown/country', async (req, res) => {
  try {
    const [rows] = await pool.query(`
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

// Export app for Vercel serverless functions
module.exports = app;

if (process.env.NODE_ENV !== 'production' && !process.env.VERCEL) {
  const PORT = process.env.PORT || 8080;
  app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
  });
}
