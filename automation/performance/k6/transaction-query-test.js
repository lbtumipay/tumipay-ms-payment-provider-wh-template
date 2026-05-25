/**
 * transaction-query-test.js — TransactionController k6 Performance Test
 *
 * Controller: TransactionController
 * Endpoint: GET /tp/payment/adapter/v1/transactions
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 *
 * Usage:
 *   k6 run automation/performance/k6/transaction-query-test.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=smoke \
 *     -e TRANSACTION_ID=txn-001 \
 *     -e ADAPTER_TRANSACTION_ID=adp-txn-001 \
 *     -e PROVIDER_TRANSACTION_ID=prov-txn-001
 *
 * Scenarios:
 *   S-01 query_smoke  — Smoke  (1 VU   / 30s)
 *   S-02 query_load   — Load   (20 VUs / 5m ramp)
 *   S-03 query_stress — Stress (up to 150 VUs)
 */

import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';
import {smokeOptions} from './config/options.js';
import {queryThresholds as thresholds} from './config/thresholds.js';
import {buildHeaders} from './utils/helpers.js';
import {buildSummaryOutputs} from './utils/summary.js';

// ── Custom metrics ────────────────────────────────────────────────────────────
const queryDuration = new Trend('query_transaction_duration');
const queryErrors = new Rate('query_transaction_errors');
const queryRequests = new Counter('query_transaction_requests');

// ── Extended load options for query endpoint (higher concurrency) ─────────────
const queryLoadOptions = {
    stages: [
        {duration: '1m', target: 20},
        {duration: '3m', target: 20},
        {duration: '1m', target: 0},
    ],
};

const queryStressOptions = {
    stages: [
        {duration: '2m', target: 75},
        {duration: '5m', target: 75},
        {duration: '2m', target: 150},
        {duration: '5m', target: 150},
        {duration: '2m', target: 0},
    ],
};

// ── Options map by test type ──────────────────────────────────────────────────
const TEST_TYPE = __ENV.TEST_TYPE || 'smoke';
const optionsMap = {
    smoke: {...smokeOptions, thresholds},
    load: {...queryLoadOptions, thresholds},
    stress: {...queryStressOptions, thresholds},
};
export const options = optionsMap[TEST_TYPE] || optionsMap.smoke;

// ── Available query parameters ────────────────────────────────────────────────
const QUERY_PARAMS = [
    {key: 'transaction_id', value: __ENV.TRANSACTION_ID || 'txn-perf-001'},
    {key: 'adapter_transaction_id', value: __ENV.ADAPTER_TRANSACTION_ID || 'adp-perf-001'},
    {key: 'provider_transaction_id', value: __ENV.PROVIDER_TRANSACTION_ID || 'prov-perf-001'},
];

// ── Main function ─────────────────────────────────────────────────────────────
export default function () {
    const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

    // Rotate through the three query params using iteration modulo
    const param = QUERY_PARAMS[__ITER % QUERY_PARAMS.length];
    const url = `${BASE_URL}/tp/payment/adapter/v1/transactions?${param.key}=${param.value}`;

    const headers = buildHeaders({
        apiKey: __ENV.API_KEY || 'test-api-key',
    });

    const startTime = Date.now();
    const response = http.get(url, {headers});
    queryDuration.add(Date.now() - startTime);
    queryRequests.add(1);

    const passed = check(response, {
        'query HTTP 200 or 4xx': (r) => r.status === 200 || (r.status >= 400 && r.status < 500),
        'query response time < 1s': (r) => r.timings.duration < 1000,
        'query valid body': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch {
                return false;
            }
        },
    });

    queryErrors.add(!passed);

    if (!passed) {
        console.error(`[ERROR] ${url} → status=${response.status} body=${response.body}`);
    }

    sleep(0.5);
}

// ── Output report: JSON + CSV ─────────────────────────────────────────────────
export function handleSummary(data) {
    return buildSummaryOutputs(data, 'query', 'automation/performance/k6/results/query');
}
