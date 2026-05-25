/**
 * payin-test.js — PayInTransactionController k6 Performance Test
 *
 * Controller: PayInTransactionController
 * Endpoint: POST /tp/payment/adapter/v1/payin/transaction
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 *
 * Usage:
 *   k6 run automation/performance/k6/payin-test.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=smoke
 *
 * Scenarios:
 *   S-01 payin_smoke  — Smoke  (1 VU  / 30s)
 *   S-02 payin_load   — Load   (10 VUs / 5m ramp)
 *   S-03 payin_stress — Stress (up to 100 VUs)
 *   S-04 payin_spike  — Spike  (100 VUs / 10s burst)
 */

import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';
import {loadOptions, smokeOptions, spikeOptions, stressOptions} from './config/options.js';
import {payinThresholds as thresholds} from './config/thresholds.js';
import {buildHeaders, parseBody, uuidv4} from './utils/helpers.js';
import {buildSummaryOutputs} from './utils/summary.js';

// ── Custom metrics ────────────────────────────────────────────────────────────
const payInDuration = new Trend('payin_transaction_duration');
const payInErrors = new Rate('payin_transaction_errors');
const payInRequests = new Counter('payin_transaction_requests');

// ── Options map by test type ──────────────────────────────────────────────────
const TEST_TYPE = __ENV.TEST_TYPE || 'smoke';
const optionsMap = {
    smoke: {...smokeOptions, thresholds},
    load: {...loadOptions, thresholds},
    stress: {...stressOptions, thresholds},
    spike: {...spikeOptions, thresholds},
};
export const options = optionsMap[TEST_TYPE] || optionsMap.smoke;

// ── Test data ─────────────────────────────────────────────────────────────────
const payloads = JSON.parse(
    open('./data/payin-payload.json')
);

// ── Main function ─────────────────────────────────────────────────────────────
export default function () {

    const BASE_URL = __ENV.BASE_URL || 'http://localhost:8000';
    const url = `${BASE_URL}/tp/payment/adapter/v1/payin/transaction`;

    // Pick a random payload and replace transaction_id with a fresh UUID
    const payload = JSON.parse(JSON.stringify(payloads[Math.floor(Math.random() * payloads.length)]));
    payload.transaction.transaction_id = uuidv4();

    const headers = buildHeaders({
        idempotency: true,
        merchantId: __ENV.MERCHANT_ID || 'test-merchant',
        apiKey: __ENV.API_KEY || 'test-api-key',
    });

    const startTime = Date.now();
    const response = http.post(url, JSON.stringify(payload), {headers});
    payInDuration.add(Date.now() - startTime);
    payInRequests.add(1);

    const passed = check(response, {
        'payin HTTP 200': (r) => r.status === 200,
        'payin code PROCESS_COMPLETED': (r) => parseBody(r).code === 'PROCESS_COMPLETED',
        'payin status SUCCESS': (r) => parseBody(r).status === 'SUCCESS',
        'payin adapter_transaction_id present': (r) => !!parseBody(r).data?.adapter_transaction_id,
        'payin response time < 2s': (r) => r.timings.duration < 2000,
    });

    payInErrors.add(!passed);

    if (!passed) {
        console.error(`[ERROR] ${url} → status=${response.status} body=${response.body}`);
    }

    sleep(1);
}

// ── Output report: JSON + CSV ─────────────────────────────────────────────────
export function handleSummary(data) {
    return buildSummaryOutputs(data, 'payin', 'automation/performance/k6/results/payin');
}
