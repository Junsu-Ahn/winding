document.addEventListener('DOMContentLoaded', function() {
    var mapElement = document.getElementById('map');

    // 출발지, 도착지, 경유지의 좌표를 가져옵니다.
    var departureLat = parseFloat(mapElement.getAttribute('data-departure-lat'));
    var departureLng = parseFloat(mapElement.getAttribute('data-departure-lng'));
    var destinationLat = parseFloat(mapElement.getAttribute('data-destination-lat'));
    var destinationLng = parseFloat(mapElement.getAttribute('data-destination-lng'));

     var waypoints;
        try {
            waypoints = JSON.parse(mapElement.getAttribute('data-waypoints'));
        } catch (e) {
            console.error('경유지 데이터를 파싱하는 중 오류가 발생했습니다:', e);
            waypoints = [];
        }

    if (isNaN(departureLat) || isNaN(departureLng) || isNaN(destinationLat) || isNaN(destinationLng)) {
        console.error('출발지 또는 도착지 좌표가 유효하지 않습니다.');
        return;
    }

    // 네이버 지도 초기화
    var map = new naver.maps.Map('map', {
        center: new naver.maps.LatLng(departureLat, departureLng),
        zoom: 10
    });

    var start = departureLng + ',' + departureLat;
    var goal = destinationLng + ',' + destinationLat;

      // 출발지 Marker 추가
        var startMarker = new naver.maps.Marker({
            position: new naver.maps.LatLng(departureLat, departureLng),
            map: map,
            title: '출발지'
        });

        // 도착지 Marker 추가
        var goalMarker = new naver.maps.Marker({
            position: new naver.maps.LatLng(destinationLat, destinationLng),
            map: map,
            title: '도착지'
        });

    // 서버로 요청을 보내기 위한 URL 생성
    var serverUrl = '/api/naver-route?start=' + start + '&goal=' + goal + '&option=trafast';

    console.log('API 요청 URL:', serverUrl); // 디버그를 위해 URL 출력

    // 서버를 통해 네이버 Directions API 경로 데이터를 요청
    fetch(serverUrl)
        .then(function(response) {
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            return response.json();
        })
        .then(function(data) {
            console.log('API 응답 데이터:', data);

            if (data.code !== 0) {
                console.error('경로 요청 실패:', data.message);
                return;
            }

            var route = null;

            // trafast 경로가 있는지 확인
            if (data.route.trafast && data.route.trafast.length > 0) {
                route = data.route.trafast[0];
            }

            if (route) {
                var path = route.path;
                var polylinePath = path.map(function(coord) {
                    return new naver.maps.LatLng(coord[1], coord[0]);
                });

                // 경로를 지도에 표시
                var polyline = new naver.maps.Polyline({
                    path: polylinePath,
                    strokeColor: '#FF0000', // 선 색상
                    strokeOpacity: 0.8, // 선 투명도
                    strokeWeight: 6,   // 선 두께
                    map: map           // 지도를 오버레이할 대상
                });

                console.log('경로 좌표 배열:', polylinePath); // 경로 좌표 배열 출력
            } else {
                console.error('유효한 경로 데이터를 찾을 수 없습니다.');
            }
        })
        .catch(function(error) {
            console.error('API 요청 오류:', error);
        });
});
