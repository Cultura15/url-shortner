import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ENDPOINT = `${BASE_URL}/api/v1/create-url`;
const LIMIT = 10; // must match Bandwidth.classic(10, ...) in RateLimitFilter

export const options = {
    scenarios: {
        rate_limit_check: {
            executor: 'shared-iterations',
            vus: 1,          // single VU = single "client" = single source IP
            iterations: LIMIT + 5, // send 5 requests past the limit
            maxDuration: '30s',
        },
    },
    thresholds: {
        // fail the whole run if these expectations aren't met
        'checks{expected_status:allowed}': ['rate==1.0'],
        'checks{expected_status:blocked}': ['rate==1.0'],
    },
};

export default function (data) {
    const iterationNumber = __ITER + 1; // k6 iterations are 0-indexed

    const payload = JSON.stringify({
        longUrl: 'https://example.com',
        expiresAt: '2027-12-31T23:59:59Z',
    });

    const res = http.post(ENDPOINT, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    if (iterationNumber <= LIMIT) {
        check(res, {
            'request within limit succeeds (201)': (r) => r.status === 201,
        }, { expected_status: 'allowed' });
    } else {
        check(res, {
            'request beyond limit is rejected (429)': (r) => r.status === 429,
        }, { expected_status: 'blocked' });
    }

    console.log(`Request ${iterationNumber}: ${res.status}`);
}