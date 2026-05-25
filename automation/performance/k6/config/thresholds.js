/**
 * thresholds.js — Global performance thresholds for TumiPay
 *
 * Reference:
 *   - p95 < 2000ms : 95% of requests must respond in under 2 seconds.
 *   - p99 < 5000ms : 99% of requests must respond in under 5 seconds.
 *   - error rate < 1% : Error rate must not exceed 1%.
 *
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 */
// Base HTTP thresholds shared by all scripts
const baseThresholds = {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.01'],
};

// Full thresholds used only when ALL scripts run together (main.js)
export const thresholds = {
    ...baseThresholds,

    // PayIn scenario metrics
    payin_transaction_duration: ['p(95)<2000'],
    payin_transaction_errors: ['rate<0.01'],

    // PayOut scenario metrics
    payout_transaction_duration: ['p(95)<2000'],
    payout_transaction_errors: ['rate<0.01'],

    // Transaction query scenario metrics (faster — GET endpoint)
    query_transaction_duration: ['p(95)<1000'],
    query_transaction_errors: ['rate<0.01'],

    // Microservice info scenario metrics (very fast)
    info_microservice_duration: ['p(95)<500'],
    info_microservice_errors: ['rate<0.005'],
};

// Per-script thresholds — only include metrics defined in that script
export const payinThresholds = {
    ...baseThresholds,
    payin_transaction_duration: ['p(95)<2000'],
    payin_transaction_errors: ['rate<0.01'],
};

export const payoutThresholds = {
    ...baseThresholds,
    payout_transaction_duration: ['p(95)<2000'],
    payout_transaction_errors: ['rate<0.01'],
};

export const queryThresholds = {
    ...baseThresholds,
    query_transaction_duration: ['p(95)<1000'],
    query_transaction_errors: ['rate<0.01'],
};

export const infoThresholds = {
    ...baseThresholds,
    info_microservice_duration: ['p(95)<500'],
    info_microservice_errors: ['rate<0.005'],
};

