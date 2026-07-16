import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'https://xn--h49ay0ly04a.xn--h32bi4v.xn--3e0b707e';

export const options = {
    // 1. HTTP/2 활성화
    protocols: ['http2'],
    stages: [
        { duration: '5s', target: 5 },
        { duration: '30s', target: 50 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
    },
};

// 각 VU(가상 사용자)별로 마지막에 받은 ETag를 저장할 맵
const etagMap = {};

export function setup() {
    const res = http.post(`${BASE_URL}/api/users/login`, JSON.stringify({
        username: "brian1205",
        password: "Pass1234!"
    }), { headers: { 'Content-Type': 'application/json' } });

    return { token: res.json().accessToken };
}

export default function (data) {
    const randomPage = Math.floor(Math.random() * 100);
    const url = `${BASE_URL}/api/exercise-records?page=${randomPage}&size=20&sort=createdAt,desc`;

    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
            'Content-Type': 'application/json',
            'Accept-Encoding': 'gzip',      // 2. Gzip 압축 요청
            'If-None-Match': etagMap[url] || '', // 3. ETag 재검증 (304 유도)
        },
    };

    const res = http.get(url, params);

    // ETag가 응답에 있다면 저장 (다음 요청 때 사용)
    if (res.headers['Etag']) {
        etagMap[url] = res.headers['Etag'];
    }

    check(res, {
        // 데이터가 변하지 않았다면 304, 변했다면 200 응답 확인
        'is success (200 or 304)': (r) => r.status === 200 || r.status === 304,
        'using http2': (r) => r.proto === 'HTTP/2.0',
    });

    sleep(1);
}