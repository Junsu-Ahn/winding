// 페이지 로드 시 게시물 데이터를 가져와서 표시
document.addEventListener('DOMContentLoaded', async function() {
    const postId = window.location.pathname.split('/').pop(); // URL에서 게시물 ID 추출
    const post = await fetchPostData(postId);

    if (post) {
        document.getElementById('postTitle').value = post.title;
        document.getElementById('postDate').value = new Date(post.createDate).toLocaleDateString();
        document.getElementById('postViews').value = `${post.views} views`;
        document.getElementById('postAuthor').value = post.author.nickname; // Member의 nickname을 사용
        document.getElementById('postContent').value = post.description;
    } else {
        // post가 null인 경우의 처리
        console.error('Post data is null.');
    }
});

// 특정 게시물 데이터를 가져오는 함수
async function fetchPostData(postId) {
    const response = await fetch(`/posts/${postId}`);
    if (response.ok) {
        return await response.json();
    } else {
        console.error('Failed to fetch post data');
        return null;
    }
}
