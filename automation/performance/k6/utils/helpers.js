/**
 * helpers.js — Shared utilities for k6 scripts
 *
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 */

/**
 * Generates a UUID v4 compatible with the k6 runtime.
 * @returns {string} UUID in format xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
 */
export function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = Math.random() * 16 | 0;
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

/**
 * Builds the standard TumiPay HTTP headers.
 * @param {object} opts Header configuration options
 * @param {boolean} [opts.idempotency=false] If true, adds X-Idempotency-Key
 * @param {string}  [opts.merchantId]        Value for X-Merchant-ID
 * @param {string}  [opts.apiKey]            Value for X-Api-Key
 * @returns {object} HTTP headers ready to use in k6
 */
export function buildHeaders({idempotency = false, merchantId, apiKey} = {}) {

    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'X-Request-ID': uuidv4(),
        'X-Correlation-ID': uuidv4(),
    };

    if (apiKey) {
        headers['X-Api-Key'] = apiKey;
    }
    if (merchantId) {
        headers['X-Merchant-ID'] = merchantId;
    }
    if (idempotency) {
        headers['X-Idempotency-Key'] = uuidv4();
    }

    return headers;
}

/**
 * Safely parses the body of a k6 response.
 * @param {object} response k6 response object
 * @returns {object} Parsed JSON or {} on error
 */
export function parseBody(response) {
    try {
        return JSON.parse(response.body);
    } catch {
        return {};
    }
}
