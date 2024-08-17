// 다음 주소 검색 API 사용
function execDaumPostcode(targetId) {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
            document.getElementById(targetId).value = addr;

            // 주소를 좌표로 변환
            getCoordinatesFromNaver(addr, targetId);
        }
    }).open();
}

// Naver 지오코딩 API를 사용하여 주소를 좌표로 변환
function getCoordinatesFromNaver(address, targetId) {
    naver.maps.Service.geocode({query: address}, function(status, response) {
        if (status === naver.maps.Service.Status.ERROR) {
            return alert('주소를 좌표로 변환하는 데 실패했습니다.');
        }

        if (response.v2.addresses.length > 0) {
            var result = response.v2.addresses[0];
            document.getElementById(targetId + 'Lat').value = result.y;
            document.getElementById(targetId + 'Lng').value = result.x;
        } else {
            alert("주소를 찾을 수 없습니다.");
        }
    });
}

let waypointCount = 0;

function addWaypoint() {
    waypointCount++;
    const waypointContainer = document.createElement('div');
    waypointContainer.className = 'form-group';
    waypointContainer.id = `waypoint-group-${waypointCount}`;

    waypointContainer.innerHTML = `
        <label for="waypoint${waypointCount}">경유지 ${waypointCount}</label>
        <input type="text" class="form-control" id="waypoint${waypointCount}" name="waypoints[${waypointCount}]" placeholder="경유지 주소" required readonly>
        <input type="hidden" id="waypoint${waypointCount}Lat" name="waypointLats[${waypointCount}]">
        <input type="hidden" id="waypoint${waypointCount}Lng" name="waypointLngs[${waypointCount}]">
        <button type="button" class="btn btn-secondary mt-2" onclick="execDaumPostcode('waypoint${waypointCount}')">주소 검색</button>
        <button type="button" class="btn btn-danger mt-2" onclick="removeWaypoint(${waypointCount})">경유지 삭제</button>
    `;

    document.getElementById('waypoints-container').appendChild(waypointContainer);
}

function removeWaypoint(id) {
    const waypointContainer = document.getElementById(`waypoint-group-${id}`);
    waypointContainer.remove();
}

// Reverse Geocoding을 사용하여 도로에 가까운 위치로 조정
function adjustToNearestRoad(lat, lng, callback) {
    const coords = `${lng},${lat}`;  // 위도와 경도를 'lng,lat' 형식으로 설정
    const orders = 'roadaddr,addr';  // 도로명 주소와 일반 주소를 요청 순서대로 설정

    naver.maps.Service.reverseGeocode({
        coords: coords,
        orders: orders
    }, function(status, response) {
        if (status === naver.maps.Service.Status.ERROR || !response.v2 || !response.v2.results) {
            alert('Reverse Geocoding 오류가 발생했습니다.');
            return callback(null);
        }

        if (response.v2.results.length > 0) {
            const roadAddress = response.v2.results.find(result => result.name === 'roadaddr');
            if (roadAddress && roadAddress.location) {
                const roadCoords = roadAddress.location;
                callback({
                    lat: roadCoords.y,
                    lng: roadCoords.x
                });
            } else {
                alert("도로 주소를 찾을 수 없습니다.");
                callback(null);
            }
        } else {
            alert("결과가 없습니다.");
            callback(null);
        }
    });
}

function submitRoute() {
    const departureLat = document.getElementById('departureLat').value;
    const departureLng = document.getElementById('departureLng').value;
    const destinationLat = document.getElementById('destinationLat').value;
    const destinationLng = document.getElementById('destinationLng').value;

    // 출발지와 도착지 좌표를 도로에 가까운 위치로 조정 후 경로 요청
    adjustToNearestRoad(departureLat, departureLng, function(adjustedStart) {
        if (!adjustedStart) {
            alert("출발지의 도로 근처 위치를 찾을 수 없습니다.");
            return;
        }

        adjustToNearestRoad(destinationLat, destinationLng, function(adjustedGoal) {
            if (!adjustedGoal) {
                alert("도착지의 도로 근처 위치를 찾을 수 없습니다.");
                return;
            }

            // 경로 요청
            const uriPath = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving";
            const start = `${adjustedStart.lng},${adjustedStart.lat}`;
            const goal = `${adjustedGoal.lng},${adjustedGoal.lat}`;
            const option = "trafast";

            fetch(`${uriPath}?start=${start}&goal=${goal}&option=${option}`, {
                method: 'GET',
                headers: {
                    'X-NCP-APIGW-API-KEY-ID': 'aun2dmrzp7',
                    'X-NCP-APIGW-API-KEY': 'oTt8xRONK83duRvoXGMKjOlQ1NYxXwPq3monIcnl'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 0) {
                    displayRouteOnMap(data.route.trafast[0].path);
                } else {
                    alert('경로를 불러오는 데 실패했습니다: ' + data.message);
                }
            })
            .catch(error => console.error('Error:', error));
        });
    });
}

function displayRouteOnMap(path) {
    clearMap();

    if (!path || path.length === 0) {
        alert('경로 데이터가 비어 있습니다.');
        return;
    }

    const pathCoords = path.map(point => new naver.maps.LatLng(point[1], point[0]));

    const polyline = new naver.maps.Polyline({
        map: map,
        path: pathCoords,
        strokeColor: '#5347AA',
        strokeWeight: 6
    });

    map.setCenter(pathCoords[0]);
}

let map;
let markers = [];

document.addEventListener("DOMContentLoaded", function() {
    map = new naver.maps.Map(document.getElementById('map'), {
        center: new naver.maps.LatLng(37.394727, 127.110153),
        zoom: 10
    });
});

function clearMap() {
    markers.forEach(marker => marker.setMap(null));
    markers = [];
}
