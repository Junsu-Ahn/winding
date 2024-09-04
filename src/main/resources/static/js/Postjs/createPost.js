$(document).ready(function () {

    // 다음 주소 검색 API를 사용하여 주소를 입력하는 함수
    window.execDaumPostcode = function(targetId) {
        new daum.Postcode({
            oncomplete: function(data) {
                // 지번 주소 또는 도로명 주소 선택
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById(targetId).value = addr;

                // 입력된 주소를 도로명 주소로 변환 시도
                convertToRoadAddress(addr, targetId);
            }
        }).open();
    };

    // 도로명 주소로 변환하는 함수
    function convertToRoadAddress(address, targetId) {
        naver.maps.Service.geocode({query: address}, function(status, response) {
            if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
                alert('해당 주소를 도로명 주소로 변환할 수 없습니다. 다시 시도합니다.');

                // 도로명 주소가 없을 경우, 지번 주소로 다시 변환 시도
                toggleAddressTypeAndRetry(address, targetId);
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
                alert('해당 주소를 도로명 주소로 변환할 수 없습니다.');
            }
        });
    }

    // 주소 타입을 반전하여 다시 변환 시도 (도로명 -> 지번, 지번 -> 도로명)
    function toggleAddressTypeAndRetry(address, targetId) {
        naver.maps.Service.geocode({query: address}, function(status, response) {
            if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
                alert('주소 변환에 실패했습니다. 다시 확인해주세요.');
                return;
            }

            // 지번 주소 또는 도로명 주소 반전
            const alternativeAddress = response.v2.addresses[0].jibunAddress || response.v2.addresses[0].roadAddress;
            if (alternativeAddress) {
                document.getElementById(targetId).value = alternativeAddress;
                convertToRoadAddress(alternativeAddress, targetId);
            } else {
                alert('주소를 변환할 수 없습니다. 올바른 주소를 입력해주세요.');
            }
        });
    }

    // 네이버 API 키를 가져오는 함수 (사용하지 않는다면 제거 가능)
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

    window.addWaypoint = function() {
        waypointCount++;
        const waypointContainer = document.createElement('div');
        waypointContainer.className = 'form-group waypoint-group';
        waypointContainer.id = `waypoint-group-${waypointCount}`;

        waypointContainer.innerHTML = `
            <label for="waypoint${waypointCount}">경유지 ${waypointCount}</label>
            <input type="text" class="form-control" id="waypoint${waypointCount}" name="waypoints[${waypointCount}]" placeholder="경유지 주소" readonly>
            <input type="hidden" id="waypoint${waypointCount}Lat" name="waypointLats[${waypointCount}]">
            <input type="hidden" id="waypoint${waypointCount}Lng" name="waypointLngs[${waypointCount}]">
            <button type="button" class="btn btn-secondary mt-2" onclick="execDaumPostcode('waypoint${waypointCount}')">주소 검색</button>
            <button type="button" class="btn btn-danger mt-2" onclick="removeWaypoint(${waypointCount})">경유지 삭제</button>
        `;

        document.getElementById('waypoints-container').appendChild(waypointContainer);
    }

    window.removeWaypoint = function(id) {
        const waypointContainer = document.getElementById(`waypoint-group-${id}`);
        if (waypointContainer) {
            waypointContainer.remove();
        }
    }

    // 사진 추가 기능
    let imageCount = 0;

    window.addImageUpload = function() {
        imageCount++;
        const imageContainer = document.createElement('div');
        imageContainer.className = 'form-group image-upload-group';
        imageContainer.id = `image-upload-group-${imageCount}`;

        imageContainer.innerHTML = `
            <label for="image${imageCount}">이미지 ${imageCount}</label>
            <input type="file" class="form-control" id="image${imageCount}" name="images[${imageCount}]">
            <input type="text" class="form-control mt-2" id="imageDescription${imageCount}" name="imageDescriptions[${imageCount}]" placeholder="이미지 설명을 입력하세요">
            <button type="button" class="btn btn-danger mt-2" onclick="removeImage(${imageCount})">이미지 삭제</button>
        `;

        document.getElementById('image-upload-container').appendChild(imageContainer);
    }

    window.removeImage = function(id) {
        const imageContainer = document.getElementById(`image-upload-group-${id}`);
        if (imageContainer) {
            imageContainer.remove();
        }
    }
});
