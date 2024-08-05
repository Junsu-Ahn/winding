document.addEventListener("DOMContentLoaded", function() {
    var container = document.getElementById('map');
    var options = {
        center: new kakao.maps.LatLng(36.5, 127.5), // 대한민국 중심 좌표
        level: 7
    };

    var map = new kakao.maps.Map(container, options);

    // 도시 목록 (예시)
    var cities = [
        { name: '서울', latlng: new kakao.maps.LatLng(37.5665, 126.9780) },
        { name: '부산', latlng: new kakao.maps.LatLng(35.1796, 129.0756) },
        { name: '대구', latlng: new kakao.maps.LatLng(35.8714, 128.6014) },
        { name: '인천', latlng: new kakao.maps.LatLng(37.4563, 126.7052) },
        { name: '광주', latlng: new kakao.maps.LatLng(35.1595, 126.8526) },
        { name: '대전', latlng: new kakao.maps.LatLng(36.3504, 127.3845) },
        { name: '울산', latlng: new kakao.maps.LatLng(35.5393, 129.3114) },
        { name: '제주', latlng: new kakao.maps.LatLng(33.4996, 126.5312) }
    ];

    // 도시 마커 생성 및 클릭 이벤트
    cities.forEach(function(city) {
        var marker = new kakao.maps.Marker({
            map: map,
            position: city.latlng,
            title: city.name
        });

        kakao.maps.event.addListener(marker, 'click', function() {
            alert(city.name + '을(를) 클릭했습니다.');
        });
    });
});
