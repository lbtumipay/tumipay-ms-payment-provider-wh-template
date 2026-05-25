/**
 * microservice-info-test.js — MicroServiceController k6 Performance Test
 *
 * Controller: MicroServiceController
 * Endpoint: GET /tp/payment/adapter/v1/microservice/info
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 *
 * Usage:
 *   k6 run automation/performance/k6/microservice-info-test.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e TEST_TYPE=smoke
 *
 * Scenarios:
 *   S-01 info_smoke — Smoke (1 VU  / 30s)
 *   S-02 info_load  — Load  (50 VUs / 3m constant)
 */

import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';
import {smokeOptions} from './config/options.js';
import {infoThresholds as thresholds} from './config/thresholds.js';
import {buildHeaders, parseBody} from './utils/helpers.js';
import {buildSummaryOutputs} from './utils/summary.js';

// ── Custom metrics ────────────────────────────────────────────────────────────
const infoDuration = new Trend('info_microservice_duration');
const infoErrors = new Rate('info_microservice_errors');
const infoRequests = new Counter('info_microservice_requests');

// ── Load option for info endpoint (high frequency — used by health checks) ────
const infoLoadOptions = {
    vus: 50, duration: '3m',
};

// ── Options map by test type ──────────────────────────────────────────────────
const TEST_TYPE = __ENV.TEST_TYPE || 'smoke';
const optionsMap = {
    smoke: {...smokeOptions, thresholds}, load: {...infoLoadOptions, thresholds},
};
export const options = optionsMap[TEST_TYPE] || optionsMap.smoke;

// ── Main function ─────────────────────────────────────────────────────────────
export default function () {

    const BASE_URL = __ENV.BASE_URL || 'http://localhost:8000';
    const url = `${BASE_URL}/tp/payment/adapter/v1/microservice/info`;

    // Info endpoint requires no authentication or special headers
    const headers = buildHeaders({});

    const startTime = Date.now();
    const response = http.get(url, {headers});
    infoDuration.add(Date.now() - startTime);
    infoRequests.add(1);

    const passed = check(response, {
        'info HTTP 200': (r) => r.status === 200,
        'info code PROCESS_COMPLETED': (r) => parseBody(r).code === 'PROCESS_COMPLETED',
        'info service_name present': (r) => !!parseBody(r).data?.service_name,
        'info response time < 500ms': (r) => r.timings.duration < 500,
    });

    infoErrors.add(!passed);

    if (!passed) {
        console.error(`[ERROR] ${url} → status=${response.status} body=${response.body}`);
    }

    sleep(1);
}

// ── Output report: JSON + CSV ─────────────────────────────────────────────────
export function handleSummary(data) {
    return buildSummaryOutputs(data, 'info', 'automation/performance/k6/results/info');
}
