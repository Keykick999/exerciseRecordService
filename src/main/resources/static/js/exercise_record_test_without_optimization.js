import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'https://xn--h49ay0ly04a.xn--h32bi4v.xn--3e0b707e';

export const options = {
    // 1. HTTP/1.1 강제 (멀티플렉싱 비활성화)
    protocols: ['http1.1'],
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
    const res = http.post(`${BASE_URL}/api/users/login`, JSON.stringify({
        username: "brian1205",
        password: "Pass1234!"
    }), { headers: { 'Content-Type': 'application/json' } });

    return { token: res.json().accessToken };
}

export default function (data) {
    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
            'Content-Type': 'application/json',
            // 2. 최적화 거부 설정
            'Accept-Encoding': 'identity', // 압축(Gzip) 사용 안 함
            'Cache-Control': 'no-cache',   // 캐시 사용 안 함
        },
    };

    const randomPage = Math.floor(Math.random() * 100);
    const res = http.get(`${BASE_URL}/api/exercise-records?page=${randomPage}&size=20&sort=createdAt,desc`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'no-compression': (r) => r.headers['Content-Encoding'] === undefined,
    });

    sleep(1);
}