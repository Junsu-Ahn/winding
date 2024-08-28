// 경로 데이터를 가져오는 함수
async function getRoute(startLocation, goalLocation, waypoints) {
    const payload = {
        startLocation: startLocation,
        goalLocation: goalLocation,
        waypoints: waypoints
    };

    try {
        const response = await fetch('/api/calculate-route', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Received route data:', data);
            return data;
        } else {
            console.error('Failed to fetch route data:', response.status, response.statusText);
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

    var waypoints = mapElement.getAttribute('data-waypoints') ? mapElement.getAttribute('data-waypoints').split('|') : [];
    var waypointLats = mapElement.getAttribute('data-waypoint-lats') ? mapElement.getAttribute('data-waypoint-lats').split('|').map(parseFloat) : [];
    var waypointLngs = mapElement.getAttribute('data-waypoint-lngs') ? mapElement.getAttribute('data-waypoint-lngs').split('|').map(parseFloat) : [];

    if (!isNaN(departureLat) && !isNaN(departureLng) && !isNaN(destinationLat) && !isNaN(destinationLng)) {
        var map = new naver.maps.Map('map', {
            center: new naver.maps.LatLng(departureLat, departureLng), // 출발지 중심으로 설정
            zoom: 10
        });

        try {
            const startLocation = { lat: departureLat, lng: departureLng };
            const goalLocation = { lat: destinationLat, lng: destinationLng };

            const waypointLocations = waypoints.map((waypoint, index) => ({
                lat: waypointLats[index],
                lng: waypointLngs[index]
            }));

            const routeData = await getRoute(startLocation, goalLocation, waypointLocations);

            if (routeData && routeData.route && routeData.route.trafast && routeData.route.trafast.length > 0) {
                const routePath = routeData.route.trafast[0].path.map(coord => new naver.maps.LatLng(coord[1], coord[0]));

                new naver.maps.Polyline({
                    path: routePath,
                    strokeColor: '#5347AA',
                    strokeOpacity: 0.8,
                    strokeWeight: 6,
                    map: map
                });

                map.setCenter(routePath[0]);
            } else {
                console.error('경로 데이터를 찾을 수 없습니다.');
            }
        } catch (error) {
            console.error('경로 데이터를 가져오는 중 오류가 발생했습니다:', error);
        }
    } else {
        console.error('유효하지 않은 주소입니다.');
    }
});
