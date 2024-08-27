// Naver API 키를 가져오는 함수
async function getNaverApiKeys() {
    try {
        const response = await fetch('/api/naver');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const credentials = await response.json();
        return credentials;
    } catch (error) {
        console.error('Error fetching Naver API keys:', error);
        throw error; // 필요한 경우, 에러를 상위로 전파
    }
}

// 경로 데이터를 가져오는 함수
async function getRoute(departureLat, departureLng, destinationLat, destinationLng, waypoints = []) {
    const start = '${departureLng},${departureLat}';
    const goal = '${destinationLng},${destinationLat}';

    let url = '/api/naver-route?start=${encodeURIComponent(start)}&goal=${encodeURIComponent(goal)}';

    if (waypoints.length > 0) {
        const waypointsParam = waypoints.map(point => '${point.lng},${point.lat}').join('|');
        url += '&waypoints=${encodeURIComponent(waypointsParam)}';
    }

    try {
        const response = await fetch(url);
        if (response.ok) {
            const data = await response.json();
            console.log('Route data:', data); // 전체 데이터를 출력해서 구조를 확인

            // 경로 데이터가 있는지 확인하고 적절하게 접근
            if (data.route && data.route.trafast && data.route.trafast.length > 0) {
                return data.route.trafast[0].path;  // 경로 데이터 참조
            } else {
                console.error('No trafast routes found.');
                return null;
            }
        } else {
            console.error('Failed to fetch route data');
            return null;
        }
    } catch (error) {
        console.error('Error fetching route data:', error);
        return null;
    }
}

// DOM이 준비되었을 때 실행되는 코드
document.addEventListener('DOMContentLoaded', async function() {
    var mapElement = document.getElementById('map');
    var departureLat = parseFloat(mapElement.getAttribute('data-departure-lat'));
    var departureLng = parseFloat(mapElement.getAttribute('data-departure-lng'));
    var destinationLat = parseFloat(mapElement.getAttribute('data-destination-lat'));
    var destinationLng = parseFloat(mapElement.getAttribute('data-destination-lng'));

    var waypoints = []; // 경유지

    if (!isNaN(departureLat) && !isNaN(departureLng) && !isNaN(destinationLat) && !isNaN(destinationLng)) {
        var map = new naver.maps.Map('map', {
            center: new naver.maps.LatLng(departureLat, departureLng),
            zoom: 10
        });

        var departureMarker = new naver.maps.Marker({
            position: new naver.maps.LatLng(departureLat, departureLng),
            map: map,
            title: '출발지'
        });

        var destinationMarker = new naver.maps.Marker({
            position: new naver.maps.LatLng(destinationLat, destinationLng),
            map: map,
            title: '도착지'
        });

        const routePath = await getRoute(departureLat, departureLng, destinationLat, destinationLng, waypoints);

        if (routePath) {
            // 경로 데이터를 Naver 지도에서 사용할 수 있는 LatLng 객체로 변환
            const pathCoordinates = routePath.map(coord => new naver.maps.LatLng(coord[1], coord[0])); // 위도와 경도를 올바르게 매핑
            drawRouteOnMap(pathCoordinates, map); // 경로를 지도에 그리는 함수 호출
        } else {
            console.error('경로 데이터가 없습니다.');
        }
    } else {
        console.error('Invalid coordinates.');
    }
});

function drawRouteOnMap(routePath, map) {
    // 경로 데이터를 Naver 지도에서 사용할 수 있는 LatLng 객체 배열로 변환
    const pathCoordinates = routePath.map(coord => new naver.maps.LatLng(coord[1], coord[0]));

    // 경로가 끊기지 않도록 폴리라인 생성
    const polyline = new naver.maps.Polyline({
        path: pathCoordinates,
        strokeColor: '#FF0000', // 선 색상

        strokeWeight: 5,        // 선 두께
        map: map
    });

    // 지도의 중심을 경로의 첫 번째 좌표로 설정
    if (pathCoordinates.length > 0) {
        map.setCenter(pathCoordinates[0]);
    }

    // 경로가 끊기지 않도록 모든 좌표를 순차적으로 연결
    for (let i = 0; i < pathCoordinates.length - 1; i++) {
        new naver.maps.Polyline({
            path: [pathCoordinates[i], pathCoordinates[i + 1]], // 각 좌표를 순차적으로 연결
            strokeColor: '#FF0000',
            strokeOpacity: 0.8,
            strokeWeight: 5,
            map: map
        });
    }
}
