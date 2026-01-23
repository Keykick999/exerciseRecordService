import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'https://xn--h49ay0ly04a.xn--h32bi4v.xn--3e0b707e';

export const options = {
    stages: [
        { duration: '5s', target: 5 },
        { duration: '30s', target: 50 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
    },
};

export function setup() {
    const loginUrl = `${BASE_URL}/api/users/login`;
    const payload = JSON.stringify({
        username: "brian1205",
        password: "Pass1234!"
    });
    const params = { headers: { 'Content-Type': 'application/json' } };
    const res = http.post(loginUrl, payload, params);

    const body = res.json();
    return { token: body.accessToken };
}

export default function (data) {
    const accessToken = data.token;
    const params = {
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
    };

    // [확인된 ID 리스트]
    const validIds = [
        985569, 896120, 806671, 717222, 627773,
        538324, 448875, 359426, 269977, 180528,
        91079, 1630, 975592, 886143, 796694,
        707245, 617796, 528347, 438898, 349449
    ];

    // 배열 내에서 랜덤하게 하나 선택
    const recordId = validIds[Math.floor(Math.random() * validIds.length)];

    const res = http.get(`${BASE_URL}/api/exercise-records/${recordId}/details`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'detail_duration < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}