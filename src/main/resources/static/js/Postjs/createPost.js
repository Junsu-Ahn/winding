// 다음 주소 검색 API 사용
function execDaumPostcode(targetId) {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
            document.getElementById(targetId).value = addr;

            // 주소를 좌표로 변환
            getCoordinatesFromKakao(addr, targetId);
        }
    }).open();
}

// 카카오 지오코딩 API를 사용하여 주소를 좌표로 변환
function getCoordinatesFromKakao(address, targetId) {
    var geocoder = new kakao.maps.services.Geocoder();

    geocoder.addressSearch(address, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
            var coords = result[0];
            document.getElementById(targetId + 'Lat').value = coords.y;
            document.getElementById(targetId + 'Lng').value = coords.x;
        } else {
            alert("주소를 좌표로 변환하는 데 실패했습니다.");
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

function submitRoute() {
    const formElement = document.getElementById('routeForm');
    const formData = new FormData(formElement);
    const jsonData = {};
    formData.forEach((value, key) => jsonData[key] = value);

    console.log("Sending data to API:", jsonData); // 데이터를 콘솔에 로그 출력

    // 지도를 초기화합니다.
    clearMap();

    fetch('/api/route', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(jsonData),
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(data => {
        console.log(data);
        displayRouteOnMap(data);
    })
    .catch(error => {
        console.error('Error:', error);
        alert("경로를 불러오는 중 오류가 발생했습니다.");
    });
}

let map;
let markers = [];

document.addEventListener("DOMContentLoaded", function() {
    map = new kakao.maps.Map(document.getElementById('map'), {
        center: new kakao.maps.LatLng(37.394727, 127.110153),
        level: 10
    });
});

function clearMap() {
    markers.forEach(marker => marker.setMap(null));
    markers = [];
}

function displayRouteOnMap(data) {
    const locations = [];

    const departureLat = parseFloat(document.getElementById('departureLat').value);
    const departureLng = parseFloat(document.getElementById('departureLng').value);
    if (!isNaN(departureLat) && !isNaN(departureLng)) {
        locations.push({ lat: departureLat, lng: departureLng });
    }

    const destinationLat = parseFloat(document.getElementById('destinationLat').value);
    const destinationLng = parseFloat(document.getElementById('destinationLng').value);
    if (!isNaN(destinationLat) && !isNaN(destinationLng)) {
        locations.push({ lat: destinationLat, lng: destinationLng });
    }

    for (let i = 1; i <= waypointCount; i++) {
        const waypointLat = parseFloat(document.getElementById(`waypoint${i}Lat`).value);
        const waypointLng = parseFloat(document.getElementById(`waypoint${i}Lng`).value);
        if (!isNaN(waypointLat) && !isNaN(waypointLng)) {
            locations.push({ lat: waypointLat, lng: waypointLng });
        }
    }

    locations.forEach(location => {
        const marker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(location.lat, location.lng),
            map: map
        });
        markers.push(marker);
    });

    if (locations.length > 0) {
        const bounds = new kakao.maps.LatLngBounds();
        locations.forEach(location => {
            bounds.extend(new kakao.maps.LatLng(location.lat, location.lng));
        });
        map.setBounds(bounds);
    }
}
