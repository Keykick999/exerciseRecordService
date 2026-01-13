import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. 공통 변수 설정
const BASE_URL = 'https://xn--h49ay0ly04a.xn--h32bi4v.xn--3e0b707e';

export const options = {
    stages: [
        { duration: '5s', target: 5 },   // 웜업: 5초 동안 5명
        { duration: '30s', target: 50 }, // 부하: 30초 동안 50명까지 증가
        { duration: '10s', target: 0 },  // 쿨다운
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% 요청이 0.5초 이내여야 성공
    },
};

// 2. setup(): 테스트 시작 전 딱 한 번 실행 (로그인 & 토큰 발급)
export function setup() {
    const loginUrl = `${BASE_URL}/api/users/login`;
    const payload = JSON.stringify({
        username: "brian1205",
        password: "Pass1234!"
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(loginUrl, payload, params);

    // 로그인이 제대로 됐는지 확인
    check(res, {
        'login status is 200': (r) => r.status === 200,
    });

    // 응답 JSON에서 accessToken 추출
    // (만약 응답 구조가 다르다면 이 부분을 수정해야 해)
    const body = res.json();
    const token = body.accessToken;

    console.log(`Access Token 획득 성공: ${token.substring(0, 10)}...`);

    // 여기서 리턴한 값은 default function의 인자로 전달됨
    return { token: token };
}

// 3. 실제 부하 테스트 (setup에서 받은 토큰 사용)
export default function (data) {
    // setup()에서 넘겨준 토큰을 꺼내서 사용
    const accessToken = data.token;

    const params = {
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
    };

    // 조회 요청 (최신순 정렬)
    const res = http.get(`${BASE_URL}/api/exercise-records?page=0&size=20&sort=createdAt,desc`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'duration < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}