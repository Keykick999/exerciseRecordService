// /js/auth.js
function renderAuthMenu() {
    const token = localStorage.getItem('jwtToken');
    const authMenu = $('#authMenu');

    if (token) {
        authMenu.html(`
            <li class="nav-item">
                <a class="nav-link" href="#" id="logoutBtn">
                    <i class="bi bi-box-arrow-right" style="font-size: 1.4rem;"></i> 로그아웃
                </a>
            </li>
        `);
        $('#logoutBtn').on('click', function (e) {
            e.preventDefault();
            localStorage.removeItem('jwtToken');
            alert("로그아웃 되었습니다.");
            window.location.href = '/login';
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

