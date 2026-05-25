/**
 * payout-test.js — PayOutTransactionController k6 Performance Test
 *
 * Controller: PayOutTransactionController
 * Endpoint: POST /tp/payment/adapter/v1/payout/transaction
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 *
 * Usage:
 *   k6 run automation/performance/k6/payout-test.js \
 *     -e BASE_URL=http://localhost:8000 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=smoke
 *
 * Scenarios:
 *   S-01 payout_smoke  — Smoke  (1 VU  / 30s)
 *   S-02 payout_load   — Load   (10 VUs / 5m ramp)
 *   S-03 payout_stress — Stress (up to 100 VUs)
 *   S-04 payout_spike  — Spike  (100 VUs / 10s burst)
 */

import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';
import {loadOptions, smokeOptions, spikeOptions, stressOptions} from './config/options.js';
import {payoutThresholds as thresholds} from './config/thresholds.js';
import {buildHeaders, parseBody, uuidv4} from './utils/helpers.js';
import {buildSummaryOutputs} from './utils/summary.js';

// ── Custom metrics ────────────────────────────────────────────────────────────
const payoutDuration = new Trend('payout_transaction_duration');
const payoutErrors = new Rate('payout_transaction_errors');
const payoutRequests = new Counter('payout_transaction_requests');

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
const payloads = JSON.parse(open('./data/payout-payload.json'));

// ── Main function ─────────────────────────────────────────────────────────────
export default function () {

    const BASE_URL = __ENV.BASE_URL || 'http://localhost:8000';
    const url = `${BASE_URL}/tp/payment/adapter/v1/payout/transaction`;

    // Pick a random payload and replace transaction_id with a fresh UUID
    const payload = JSON.parse(
        JSON.stringify(payloads[Math.floor(Math.random() * payloads.length)])
    );

    payload.transaction.transaction_id = uuidv4();

    const headers = buildHeaders({
        idempotency: true,
        merchantId: __ENV.MERCHANT_ID || 'test-merchant',
        apiKey: __ENV.API_KEY || 'test-api-key',
    });

    const startTime = Date.now();
    const response = http.post(url, JSON.stringify(payload), {headers});
    payoutDuration.add(Date.now() - startTime);
    payoutRequests.add(1);

    const passed = check(response, {
        'payout HTTP 200': (r) => r.status === 200,
        'payout code PROCESS_COMPLETED': (r) => parseBody(r).code === 'PROCESS_COMPLETED',
        'payout status SUCCESS': (r) => parseBody(r).status === 'SUCCESS',
        'payout adapter_transaction_id present': (r) => !!parseBody(r).data?.adapter_transaction_id,
        'payout response time < 2s': (r) => r.timings.duration < 2000,
    });

    payoutErrors.add(!passed);

    if (!passed) {
        console.error(`[ERROR] ${url} → status=${response.status} body=${response.body}`);
    }

    sleep(1);
}

// ── Output report: JSON + CSV ─────────────────────────────────────────────────
export function handleSummary(data) {
    return buildSummaryOutputs(data, 'payout', 'automation/performance/k6/results/payout');
}
