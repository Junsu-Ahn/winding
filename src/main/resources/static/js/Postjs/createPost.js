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
