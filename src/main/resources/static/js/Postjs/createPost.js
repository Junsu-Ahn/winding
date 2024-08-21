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

// 역지오코딩을 통해 근처 주소를 찾는 메서드
function reverseGeocodeNearby(headers, callback, targetLat, targetLng) {
    naver.maps.Service.reverseGeocode({
        coords: new naver.maps.LatLng(targetLat, targetLng),
    }, function(status, response) {
        if (status !== naver.maps.Service.Status.OK) {
            console.error('Reverse geocoding failed:', status);
            return callback(null);
        }

        var result = response.v2,
            items = result.results;

        if (items.length > 0) {
            callback(items[0].region);
        } else {
            callback(null);
        }
    });
}

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


// Naver 지오코딩 API를 사용하여 주소를 좌표로 변환하는 함수
async function getCoordinatesFromNaver(address, targetId) {
    const credentials = await getNaverApiKeys();
    const headers = {
        'X-NCP-APIGW-API-KEY-ID': credentials.clientId,
        'X-NCP-APIGW-API-KEY': credentials.clientSecret
    };

    naver.maps.Service.geocode({query: address}, function(status, response) {
        if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
            alert('주소를 좌표로 변환하는 데 실패했습니다. 근처 주소로 재시도합니다.');

            // 좌표 변환 실패 시 reverseGeocodeNearby 메서드 실행
            // 사용자에게 좌표를 직접 입력받기 위한 방법으로 수정
            navigator.geolocation.getCurrentPosition(function(position) {
                reverseGeocodeNearby(headers, function(region) {
                    if (region) {
                        const nearbyAddress = region.area1.name + ' ' + region.area2.name + ' ' + region.area3.name;
                        document.getElementById(targetId).value = nearbyAddress;

                        // 근처 주소를 사용해 다시 좌표 변환 시도
                        naver.maps.Service.geocode({query: nearbyAddress}, function(newStatus, newResponse) {
                            if (newStatus !== naver.maps.Service.Status.OK || newResponse.v2.addresses.length === 0) {
                                alert("근처 주소를 사용해도 좌표 변환에 실패했습니다. 사용자가 입력한 주소를 다시 확인해주세요.");
                            } else {
                                var result = newResponse.v2.addresses[0];
                                document.getElementById(targetId + 'Lat').value = result.y;
                                document.getElementById(targetId + 'Lng').value = result.x;
                            }
                        });
                    } else {
                        alert("근처에서 유효한 주소를 찾을 수 없습니다. 입력한 주소를 다시 확인해주세요.");
                    }
                }, position.coords.latitude, position.coords.longitude);
            });
            return;
        }

        if (response.v2.addresses.length > 0) {
            var result = response.v2.addresses[0];
            document.getElementById(targetId + 'Lat').value = result.y;
            document.getElementById(targetId + 'Lng').value = result.x;
        }

        // 입력한 주소는 그대로 유지
        document.getElementById(targetId).value = address;
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
