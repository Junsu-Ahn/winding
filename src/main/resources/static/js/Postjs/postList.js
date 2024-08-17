document.addEventListener('DOMContentLoaded', function() {
    const posts = []; // 여기에 서버에서 받아올 실제 데이터가 들어갈 것입니다.

    function loadPosts() {
         //서버에서 데이터를 가져오는 코드 (예: fetch API 사용)
         fetch('/api/posts')
             .then(response => response.json())
             .then(data => {
                 posts.push(...data);
                 renderPosts();
                 renderPagination();
             });

        // 테스트 데이터로 가정
        for (let i = 1; i <= 10; i++) {
            posts.push({
                title: `테스트 제목 ${i}`,
                views: Math.floor(Math.random() * 100),
                thumbnail: 'https://github.com/user-attachments/assets/415b30b9-749d-47bd-b20d-e00b28414cc2' // 메인 로고를 썸네일로 사용
            });
        }
        renderPosts();
        renderPagination();
    }

    function renderPosts() {
        const gridContainer = document.querySelector('.post-grid');
        if (!gridContainer) {
            console.error('Error: .post-grid element not found');
            return;
        }
        gridContainer.innerHTML = '';

        posts.forEach(post => {
            const postDiv = document.createElement('div');
            postDiv.className = 'post-item';
            postDiv.innerHTML = `
                <div class="post-thumbnail">
                    <img src="${post.thumbnail}" alt="썸네일">
                </div>
                <div class="post-info">
                    <div class="post-title">${post.title}</div>
                    <div class="post-views">조회수: ${post.views}</div>
                </div>
            `;
            gridContainer.appendChild(postDiv);
        });
    }

    function renderPagination() {
        const pagination = document.querySelector('.pagination');
        if (!pagination) {
            console.error('Error: .pagination element not found');
            return;
        }
        pagination.innerHTML = `
            <a href="#">&laquo;</a>
            <a href="#">&#8249;</a>
            <a href="#" class="active">1</a>
            <a href="#">2</a>
            <a href="#">3</a>
            <a href="#">&#8250;</a>
            <a href="#">&raquo;</a>
        `;
    }

    loadPosts();
});
