/**
 * summary.js — End-of-test report generators for k6
 *
 * Produces two output formats after each test run:
 *   - JSON : full summary data (metrics, checks, thresholds)
 *   - CSV  : flat tabular metrics for Excel / BI analysis
 *
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 */

import {textSummary} from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

/**
 * Converts the k6 summary object into CSV text.
 * Columns: script, metric, type, avg_ms, min_ms, med_ms, max_ms, p90_ms, p95_ms, p99_ms, count, rate, passes, fails
 *
 * @param {object} data  k6 summary object (received in handleSummary)
 * @param {string} label Script label (e.g. 'payin', 'payout')
 * @returns {string} CSV content ready to write to file
 */
export function generateCsvSummary(data, label) {
    const lines = [];

    // Header row
    lines.push([
        'script',
        'metric',
        'type',
        'avg_ms',
        'min_ms',
        'med_ms',
        'max_ms',
        'p90_ms',
        'p95_ms',
        'p99_ms',
        'count',
        'rate',
        'passes',
        'fails',
    ].join(','));

    const metrics = data.metrics || {};

    for (const [name, metric] of Object.entries(metrics)) {
        const v = metric.values || {};
        const type = metric.type || '';

        const row = [
            label,
            name,
            type,
            fmt(v.avg),
            fmt(v.min),
            fmt(v.med),
            fmt(v.max),
            fmt(v['p(90)']),
            fmt(v['p(95)']),
            fmt(v['p(99)']),
            fmt(v.count),
            fmt(v.rate),
            fmt(v.passes),
            fmt(v.fails),
        ];

        lines.push(row.join(','));
    }

    return lines.join('\n');
}

/**
 * Builds the standard return object for handleSummary with JSON, CSV
 * and colored console text output.
 *
 * @param {object} data      k6 summary object
 * @param {string} label     Script label (e.g. 'payin', 'payout')
 * @param {string} outputDir Output directory (no trailing slash)
 * @returns {object} File-to-content map for k6
 */
export function buildSummaryOutputs(data, label, outputDir) {

    const dir = outputDir || `automation/performance/k6/results/${label}`;
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
    const jsonFile = `${dir}/${label}-summary-${timestamp}.json`;
    const csvFile = `${dir}/${label}-summary-${timestamp}.csv`;

    return {
        [jsonFile]: JSON.stringify(data, null, 2),
        [csvFile]: generateCsvSummary(data, label),
        stdout: textSummary(data, {indent: ' ', enableColors: true}),
    };
}

// ── Internal helpers ──────────────────────────────────────────────────────────

function fmt(value) {
    if (value === undefined || value === null) return '';
    if (typeof value === 'number') {
        return Number.isInteger(value) ? value : value.toFixed(3);
    }
    return value;
}
