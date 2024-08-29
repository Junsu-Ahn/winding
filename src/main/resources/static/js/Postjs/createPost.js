// 다음 주소 검색 API를 사용하여 주소를 입력하는 함수
function execDaumPostcode(targetId) {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
            document.getElementById(targetId).value = addr;

            // 도로명 주소가 아닌 경우 도로명 주소로 변환
            if (data.userSelectedType !== 'R') {
                alert('도로명 주소가 아닌 경우 도로명 주소로 변환합니다.');
                convertToRoadAddress(addr, targetId);
            } else {
                // 좌표를 도로명 주소로 변환
                getCoordinatesFromNaver(addr, targetId);
            }
        }
    }).open();
}

// 도로명 주소로 변환하는 함수
function convertToRoadAddress(address, targetId) {
    naver.maps.Service.geocode({query: address}, function(status, response) {
        if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
            alert('해당 주소를 도로명 주소로 변환할 수 없습니다.');
            return;
        }

        // 도로명 주소가 있을 경우 변환
        const roadAddress = response.v2.addresses[0].roadAddress;
        if (roadAddress) {
            document.getElementById(targetId).value = roadAddress;
            // 도로명 주소의 좌표를 설정
            document.getElementById(targetId + 'Lat').value = response.v2.addresses[0].y;
            document.getElementById(targetId + 'Lng').value = response.v2.addresses[0].x;
        } else {
            alert('해당 주소를 도로명 주소로 변환할 수 없습니다. 올바른 주소를 입력해주세요.');
        }
    });
}

// Naver 지오코딩 API를 사용하여 주소를 좌표로 변환하는 함수
async function getCoordinatesFromNaver(address, targetId) {
    try {
        const credentials = await getNaverApiKeys();
        const headers = {
            'X-NCP-APIGW-API-KEY-ID': credentials.clientId,
            'X-NCP-APIGW-API-KEY': credentials.clientSecret
        };

        naver.maps.Service.geocode({ query: address }, function(status, response) {
            if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
                alert('주소를 좌표로 변환하는 데 실패했습니다. 근처 주소로 재시도합니다.');
                attemptNearbyAddress(targetId, headers);
                return;
            }

            var result = response.v2.addresses[0];
            document.getElementById(targetId + 'Lat').value = result.y;
            document.getElementById(targetId + 'Lng').value = result.x;

            // 입력한 주소는 그대로 유지
            document.getElementById(targetId).value = address;
        });
    } catch (error) {
        console.error('Error during geocoding:', error);
    }
}

// 좌표를 근거로 역지오코딩하여 근처 주소를 찾는 함수
function attemptNearbyAddress(targetId, headers) {
    navigator.geolocation.getCurrentPosition(function(position) {
        const { latitude, longitude } = position.coords;

        naver.maps.Service.reverseGeocode({
            coords: new naver.maps.LatLng(latitude, longitude)
        }, function(status, response) {
            if (status !== naver.maps.Service.Status.OK || response.v2.results.length === 0) {
                alert("근처에서 유효한 주소를 찾을 수 없습니다. 입력한 주소를 다시 확인해주세요.");
                return;
            }

            const region = response.v2.results[0].region;
            const nearbyAddress = '${region.area1.name} ${region.area2.name} ${region.area3.name}';
            document.getElementById(targetId).value = nearbyAddress;

            // 근처 주소를 사용해 다시 좌표 변환 시도
            naver.maps.Service.geocode({ query: nearbyAddress }, function(newStatus, newResponse) {
                if (newStatus !== naver.maps.Service.Status.OK || newResponse.v2.addresses.length === 0) {
                    alert("근처 주소를 사용해도 좌표 변환에 실패했습니다. 사용자가 입력한 주소를 다시 확인해주세요.");
                } else {
                    var result = newResponse.v2.addresses[0];
                    document.getElementById(targetId + 'Lat').value = result.y;
                    document.getElementById(targetId + 'Lng').value = result.x;
                }
            });
        });
    }, function(error) {
        console.error('Geolocation error:', error);
        alert("현재 위치를 가져오는 데 실패했습니다. 위치 서비스가 활성화되어 있는지 확인하세요.");
    });
}

// 네이버 API 키를 가져오는 함수
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
        throw error;
    }
}

// 경유지 추가 기능
let waypointCount = 0;

function addWaypoint() {
    waypointCount++;
    const waypointContainer = document.createElement('div');
    waypointContainer.className = 'form-group waypoint-group';
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
    if (waypointContainer) {
        waypointContainer.remove();
    }
}