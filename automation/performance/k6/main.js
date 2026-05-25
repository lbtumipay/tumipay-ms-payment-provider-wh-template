/**
 * main.js — k6 performance test orchestrator
 *
 * Runs all performance scenarios for the tumipay-ms-payment-provider-template
 * microservice in parallel using the k6 scenarios API.
 *
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 *
 * Usage:
 *   k6 run automation/performance/k6/main.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=load
 *
 *   # With JSON output for reporting:
 *   k6 run automation/performance/k6/main.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=load \
 *     --out json=automation/performance/k6/results/result.json
 */

import {thresholds} from './config/thresholds.js';
import {loadOptions} from './config/options.js';
import {buildSummaryOutputs} from './utils/summary.js';

export {default as payInTest} from './payin-test.js';
export {default as payOutTest} from './payout-test.js';
export {default as queryTest} from './transaction-query-test.js';
export {default as infoTest} from './microservice-info-test.js';

export const options = {
    thresholds,
    scenarios: {
        payin: {
            executor: 'ramping-vus',
            exec: 'payInTest',
            stages: loadOptions.stages,
            startVUs: 0,
            startTime: '0s',
        },
        payout: {
            executor: 'ramping-vus',
            exec: 'payOutTest',
            stages: loadOptions.stages,
            startVUs: 0,
            startTime: '30s',
        },
        query: {
            executor: 'ramping-vus',
            exec: 'queryTest',
            stages: loadOptions.stages,
            startVUs: 0,
            startTime: '60s',
        },
        info: {
            executor: 'constant-vus',
            exec: 'infoTest',
            vus: 5,
            duration: '5m',
            startTime: '0s',
        },
    },
};

// ── Output report: JSON + CSV ─────────────────────────────────────────────────
export function handleSummary(data) {
    return buildSummaryOutputs(data, 'main', 'automation/performance/k6/results/main');
}
