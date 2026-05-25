/**
 * options.js — k6 load configuration definitions
 *
 * Available test types:
 *   - smoke:  1 VU / 30s  (basic verification)
 *   - load:   ramp up to 10 VUs / 5m (sustained load)
 *   - stress: ramp up to 100 VUs (breaking point)
 *   - spike:  100 VUs sudden burst / 10s (traffic spike)
 *
 * Author: TumiPay SAS — Engineering Standards Team
 * Date: 2026-04-19
 */

export const smokeOptions = {
    vus: 1,
    duration: '30s',
};

export const loadOptions = {
    stages: [
        {duration: '1m', target: 10},
        {duration: '3m', target: 10},
        {duration: '1m', target: 0},
    ],
};

export const stressOptions = {
    stages: [
        {duration: '2m', target: 50},
        {duration: '5m', target: 50},
        {duration: '2m', target: 100},
        {duration: '5m', target: 100},
        {duration: '2m', target: 0},
    ],
};

export const spikeOptions = {
    stages: [
        {duration: '10s', target: 100},
        {duration: '1m', target: 100},
        {duration: '10s', target: 0},
    ],
};
