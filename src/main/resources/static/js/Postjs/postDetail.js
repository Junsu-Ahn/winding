// 페이지 로드 시 게시물 데이터를 가져와서 표시 및 지도 초기화
document.addEventListener('DOMContentLoaded', async function() {
    const postId = window.location.pathname.split('/').pop(); // URL에서 게시물 ID 추출
    const post = await fetchPostData(postId);

    if (post) {
        // 게시물 정보를 화면에 표시
        document.getElementById('postTitle').innerText = post.title;
        document.getElementById('postDate').innerText = new Date(post.createDate).toLocaleDateString();
        document.getElementById('postViews').innerText = `${post.views} views`;
        document.getElementById('postAuthor').innerText = post.author.nickname; // Member의 nickname을 사용
        document.getElementById('postContent').innerText = post.description;
    } else {
        // post가 null인 경우의 처리
        console.error('Post data is null.');
    }
});

// 특정 게시물 데이터를 가져오는 함수
async function fetchPostData(postId) {
    try {
        const response = await fetch(`/posts/${postId}`);
        if (response.ok) {
            return await response.json();
        } else {
            console.error('Failed to fetch post data');
            return null;
        }
    } catch (error) {
        console.error('Error fetching post data:', error);
        return null;
    }
}

 document.addEventListener('DOMContentLoaded', function() {
     var mapElement = document.getElementById('map');
     var departureLat = parseFloat(mapElement.getAttribute('data-departure-lat'));
     var departureLng = parseFloat(mapElement.getAttribute('data-departure-lng'));
     var destinationLat = parseFloat(mapElement.getAttribute('data-destination-lat'));
     var destinationLng = parseFloat(mapElement.getAttribute('data-destination-lng'));

     if (!isNaN(departureLat) && !isNaN(departureLng) && !isNaN(destinationLat) && !isNaN(destinationLng)) {
         initializeMap(departureLat, departureLng, destinationLat, destinationLng);
     } else {
         console.error('Invalid coordinates.');
     }
 });

 function initializeMap(departureLat, departureLng, destinationLat, destinationLng) {

     var departureLatLng = new naver.maps.LatLng(departureLat, departureLng);
     var destinationLatLng = new naver.maps.LatLng(destinationLat, destinationLng);

     var map = new naver.maps.Map('map', {
         center: departureLatLng,
         zoom: 7
     });

     var departureMarker = new naver.maps.Marker({
         position: departureLatLng,
         map: map,
         title: '출발지'
     });

     var destinationMarker = new naver.maps.Marker({
         position: destinationLatLng,
         map: map,
         title: '목적지'
     });

     var polyline = new naver.maps.Polyline({
         path: [departureLatLng, destinationLatLng],
         strokeColor: '#FF0000',
         strokeOpacity: 0.8,
         strokeWeight: 5,
         map: map
     });

     window.addEventListener('resize', function() {
         map.setCenter(departureLatLng);
     });
 }

