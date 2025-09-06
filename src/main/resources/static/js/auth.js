// /js/auth.js
function renderAuthMenu() {
    const accessToken = localStorage.getItem('accessToken');
    const authMenu = $('#authMenu');

    if (accessToken) {
        authMenu.html(`
      <li class="nav-item">
        <a class="nav-link" href="#" id="logoutBtn">
          <i class="bi bi-box-arrow-right" style="font-size: 1.4rem;"></i> 로그아웃
        </a>
      </li>
    `);

        $('#logoutBtn').on('click', async function (e) {
            e.preventDefault();

            const refreshToken = localStorage.getItem('refreshToken');

            try {
                // refreshToken이 있으면 서버에 로그아웃 요청
                if (refreshToken) {
                    const res = await fetch('/api/users/logout', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            // 인증이 필요 없다면 Authorization 헤더는 생략해도 됨
                            'Authorization': `Bearer ${accessToken}`
                        },
                        body: JSON.stringify({ refreshToken })
                    });

                    // 204(No Content) 또는 200대면 성공으로 간주
                    if (!res.ok && res.status !== 204) {
                        // 토큰 만료/무효 등으로 4xx가 떠도 로컬 토큰은 정리해주는 편이 안전
                        console.warn('Logout API failed:', res.status, await res.text());
                    }
                }
            } catch (err) {
                console.error('Logout request error:', err);
            } finally {
                // 서버 응답과 무관하게 로컬 토큰은 제거
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                alert('로그아웃 되었습니다.');
                window.location.href = '/login';
            }
        });

    } else {
        authMenu.html(`
      <li class="nav-item">
        <a class="nav-link" href="/login">
          <i class="bi bi-box-arrow-in-right" style="font-size: 1.4rem;"></i> 로그인
        </a>
      </li>
    `);
    }
}

// 페이지 로드 시 메뉴 렌더링
document.addEventListener('DOMContentLoaded', renderAuthMenu);
