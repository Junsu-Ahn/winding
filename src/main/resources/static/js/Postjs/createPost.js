$(document).ready(function () {
    let imageCounter = 1; // 이미지 카운터 변수

    // 이미지와 설명란 추가하는 함수
    const addImageField = () => {
        const imageFieldHtml = `
        <div class="image_upload_section" data-index="${imageCounter}">
            <div class="image_upload_wrapper">
                <input type="file" name="images[${imageCounter - 1}]" accept="image/png, image/gif, image/jpeg" onchange="readURL(this, 'preview_image_${imageCounter}');">
                <img id="preview_image_${imageCounter}" src="https://via.placeholder.com/150" alt="미리보기" style="width: 182px; height: 182px; object-fit: cover; margin-top: 10px;">
            </div>
            <div class="description_section">
                <textarea name="descriptions[${imageCounter - 1}]" placeholder="이미지 설명을 입력하세요"></textarea>
            </div>
            <button type="button" class="remove_image_field" value="삭제">삭제</button>
        </div>
        `;
        $('#imageUploadContainer').append(imageFieldHtml);
        imageCounter++;
    };

    // 이미지와 설명란 추가 버튼 클릭 이벤트
    $('#addImageBtn').click(function (e) {
        e.preventDefault();
        addImageField();
    });

    // 이미지와 설명란 삭제 버튼 이벤트
    $('#imageUploadContainer').on('click', '.remove_image_field', function (e) {
        e.preventDefault();
        $(this).closest('.image_upload_section').remove();
        updateImageFieldNames();
    });

    // 미리보기 기능
    window.readURL = function (input, previewId) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();
            reader.onload = function (e) {
                $(`#${previewId}`).attr("src", e.target.result);
            };
            reader.readAsDataURL(input.files[0]);
        } else {
            $(`#${previewId}`).attr("src", 'https://via.placeholder.com/150');
        }
    };

    // 이미지 및 설명란의 name 속성을 업데이트하는 함수
    function updateImageFieldNames() {
        let currentIndex = 1;
        $('.image_upload_section').each(function () {
            $(this).attr('data-index', currentIndex);
            $(this).find('input[type="file"]').attr('name', `images[${currentIndex - 1}]`);
            $(this).find('textarea').attr('name', `descriptions[${currentIndex - 1}]`);
            $(this).find('img').attr('id', `preview_image_${currentIndex}`);
            currentIndex++;
        });
        imageCounter = currentIndex;
    }

    // 다음 주소 검색 API를 사용하여 주소를 입력하는 함수
    window.execDaumPostcode = function(targetId) {
        new daum.Postcode({
            oncomplete: function(data) {
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById(targetId).value = addr;
                convertToRoadAddress(addr, targetId);
            }
        }).open();
    };

    // 도로명 주소로 변환하는 함수
    function convertToRoadAddress(address, targetId) {
        naver.maps.Service.geocode({query: address}, function(status, response) {
            if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
                alert('해당 주소를 도로명 주소로 변환할 수 없습니다.');
                toggleAddressTypeAndRetry(address, targetId);
                return;
            }
            const roadAddress = response.v2.addresses[0].roadAddress;
            if (roadAddress) {
                document.getElementById(targetId).value = roadAddress;
                document.getElementById(targetId + 'Lat').value = response.v2.addresses[0].y;
                document.getElementById(targetId + 'Lng').value = response.v2.addresses[0].x;
            } else {
                alert('해당 주소를 도로명 주소로 변환할 수 없습니다.');
            }
        });
    }

    // 주소 타입을 반전하여 다시 변환 시도
    function toggleAddressTypeAndRetry(address, targetId) {
        naver.maps.Service.geocode({query: address}, function(status, response) {
            if (status !== naver.maps.Service.Status.OK || response.v2.addresses.length === 0) {
                alert('주소 변환에 실패했습니다.');
                return;
            }
            const alternativeAddress = response.v2.addresses[0].jibunAddress || response.v2.addresses[0].roadAddress;
            if (alternativeAddress) {
                document.getElementById(targetId).value = alternativeAddress;
                convertToRoadAddress(alternativeAddress, targetId);
            } else {
                alert('주소를 변환할 수 없습니다.');
            }
        });
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
    };

    window.removeWaypoint = function(id) {
        const waypointContainer = document.getElementById(`waypoint-group-${id}`);
        if (waypointContainer) {
            waypointContainer.remove();
        }
    };

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
    };

    window.removeImage = function(id) {
        const imageContainer = document.getElementById(`image-upload-group-${id}`);
        if (imageContainer) {
            imageContainer.remove();
        }
    };
});
