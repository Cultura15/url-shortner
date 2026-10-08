import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ENDPOINT = `${BASE_URL}/api/v1/create-url`;
const LIMIT = 10;

export const options = {
    vus: 1,
    iterations: LIMIT + 5,
    maxDuration: '30s',
};

export default function () {
    const requestNumber = __ITER + 1;

    const payload = JSON.stringify({
        longUrl: 'https://www.fredperry.com/?srsltid=AU7gw4XfNW6PBarvGHkY5OSEI2QTKWepflfaDVkW8A6AwE-7oZ587_Pg',
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    });

    const res = http.post(ENDPOINT, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const expectedStatus = requestNumber <= LIMIT ? 201 : 429;

    check(res, {
        [`request ${requestNumber} returns ${expectedStatus}`]: (r) =>
            r.status === expectedStatus,
    });

    console.log(`Request ${requestNumber}: ${res.status}`);
}