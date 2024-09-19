$(document).ready(function () {
    let imageCounter = 1; // 이미지 카운터 변수
    let waypointCount = 0; // 경유지 개수
    let waypointMarkers = []; // 경유지 마커를 저장할 배열
    let map, startMarker, goalMarker, polyline;

    // 지도 초기화 함수
    function initMap() {
        map = new naver.maps.Map('map', {
            center: new naver.maps.LatLng(37.5665, 126.9780), // 기본 위치 (서울)
            zoom: 10
        });

        // 출발지와 목적지 마커 생성
        startMarker = new naver.maps.Marker({
            map: map,
            title: '출발지'
        });

        goalMarker = new naver.maps.Marker({
            map: map,
            title: '목적지'
        });
    }

    // 지도 초기화
    initMap();

    // 이미지와 설명란 추가하는 함수
        function addImageField() {
            const imageFieldHtml = `
                <div class="image_upload_section" data-index="${imageCounter}">
                    <div class="image_upload_wrapper">
                        <input type="file" name="images[${imageCounter - 1}]" accept="image/png, image/gif, image/jpeg" onchange="readURL(this, 'preview_image_${imageCounter}');">
                        <img id="preview_image_${imageCounter}" src="https://via.placeholder.com/150" alt="미리보기" style="width: 182px; height: 182px; object-fit: cover; margin-top: 10px;">
                    </div>
                    <div class="description_section">
                        <textarea name="descriptions[${imageCounter - 1}]" placeholder="이미지 설명을 입력하세요"></textarea>
                    </div>
                    <button type="button" class="remove_image_field btn btn-danger mt-2" value="삭제">삭제</button>
                </div>
            `;
            $('#additional-images-container').append(imageFieldHtml);
            imageCounter++;
        }

        // 이미지와 설명란 추가 버튼 클릭 이벤트
            $('#addImageBtn').click(function (e) {
                e.preventDefault();
                addImageField(); // 이미지 추가 칸 생성
            });

        // 이미지와 설명란 삭제 버튼 이벤트
            $(document).on('click', '.remove_image_field', function (e) {
                e.preventDefault();
                $(this).closest('.image_upload_section').remove();
                updateImageFieldNames();
            });

        // 이미지 미리보기 기능
            window.readURL = function (input, previewId) {
                if (input.files && input.files[0]) {
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        $(`#${previewId}`).attr("src", e.target.result);
                    };
                    reader.readAsDataURL(input.files[0]);
                } else {
                    $(`#${previewId}`).attr('src', 'https://via.placeholder.com/150');
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
                updateMap(); // 주소 입력 완료 시 지도 업데이트
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

    // 경유지 추가 함수
    window.addWaypoint = function() {
        if (waypointCount < 3) {
            waypointCount++;
            const waypointHtml = `
                <div class="waypoint-group" id="waypoint-group-${waypointCount}">
                    <label for="waypoint${waypointCount}">경유지 ${waypointCount}</label>
                    <input type="text" class="form-control" id="waypoint${waypointCount}" name="waypoints[]" placeholder="경유지 주소" readonly>
                    <input type="hidden" id="waypoint${waypointCount}Lat" name="waypointLats[]">
                    <input type="hidden" id="waypoint${waypointCount}Lng" name="waypointLngs[]">
                    <button type="button" class="btn btn-secondary mt-2" onclick="execDaumPostcode('waypoint${waypointCount}')">주소 검색</button>
                    <button type="button" class="btn btn-danger mt-2 remove-waypoint-btn" data-id="${waypointCount}">경유지 삭제</button>
                </div>`;
            $('#waypoints-container').append(waypointHtml);
        } else {
            alert('경유지는 최대 3개까지 추가할 수 있습니다.');
        }
    };

    // 경유지 삭제 함수
    $(document).on('click', '.remove-waypoint-btn', function () {
        const id = $(this).data('id');
        $(`#waypoint-group-${id}`).remove();
        waypointMarkers[id - 1]?.setMap(null); // 지도에서 마커 삭제
        waypointMarkers[id - 1] = null; // 마커 배열에서 제거
        waypointCount--;

        updateMap(); // 경유지 삭제 후 경로 재계산
    });

    // 경로 업데이트 함수
        function updateMap() {
            let departureLat = parseFloat($('#departureLat').val());
            let departureLng = parseFloat($('#departureLng').val());
            let destinationLat = parseFloat($('#destinationLat').val());
            let destinationLng = parseFloat($('#destinationLng').val());

            if (isNaN(departureLat) || isNaN(departureLng) || isNaN(destinationLat) || isNaN(destinationLng)) {
                console.error('좌표가 유효하지 않습니다.');
                return;
            }

            let waypoints = []; // 경유지 좌표 저장

            // 경유지의 위도/경도를 배열에 추가
            $('.waypoint-group').each(function (index) {
                const waypointLat = parseFloat($(this).find('input[name="waypointLats[]"]').val());
                const waypointLng = parseFloat($(this).find('input[name="waypointLngs[]"]').val());

                if (!isNaN(waypointLat) && !isNaN(waypointLng)) {
                    waypoints.push(new naver.maps.LatLng(waypointLat, waypointLng));

                    // 경유지 마커 추가 또는 업데이트
                    if (!waypointMarkers[index]) {
                        waypointMarkers[index] = new naver.maps.Marker({
                            map: map,
                            position: waypoints[index],
                            title: `경유지 ${index + 1}`
                        });
                    } else {
                        waypointMarkers[index].setPosition(waypoints[index]);
                    }
                }
            });

            // 출발지 및 목적지 마커 위치 설정
            startMarker.setPosition(new naver.maps.LatLng(departureLat, departureLng));
            goalMarker.setPosition(new naver.maps.LatLng(destinationLat, destinationLng));

            // 경로를 포함한 경로 요청
            let waypointsStr = waypoints.map(latlng => `${latlng.lng()},${latlng.lat()}`).join('|');

            let url = `/api/naver-route?start=${departureLng},${departureLat}&goal=${destinationLng},${destinationLat}&option=trafast`;
            if (waypointsStr) {
                url += `&waypoints=${waypointsStr}`;
            }

            // 네이버 경로 API 호출하여 경로 그리기
            fetch(url)
                .then(response => response.json())
                .then(data => {
                    if (data.route.trafast && data.route.trafast.length > 0) {
                        const route = data.route.trafast[0];
                        const path = route.path.map(coord => new naver.maps.LatLng(coord[1], coord[0]));

                        // 경로 Polyline 그리기
                        if (!polyline) {
                            polyline = new naver.maps.Polyline({
                                path: path,
                                strokeColor: '#FF0000',
                                strokeOpacity: 0.8,
                                strokeWeight: 6,
                                map: map
                            });
                        } else {
                            polyline.setPath(path);
                        }

                        // 경로에 맞게 지도를 확대
                        const bounds = new naver.maps.LatLngBounds();
                        path.forEach(point => bounds.extend(point));
                        map.fitBounds(bounds);
                    }
                })
                .catch(error => console.error('경로 요청 실패:', error));
        }

        // 게시글 등록 시 지도 캡처 후 썸네일로 설정
            $('#routeForm').submit(function (e) {
                e.preventDefault(); // 기본 제출 동작 방지

                captureMap().then(() => {
                    console.log('지도 캡처 후 폼 제출');
                    this.submit(); // 폼 제출
                }).catch((error) => {
                    console.error('폼 제출 전에 오류가 발생했습니다:', error);
                });
            });

    // 썸네일이 없을 경우 지도를 캡처하여 썸네일로 설정하는 함수
        function captureMap() {
            const thumbnailInput = document.getElementById('thumbnail');
            if (thumbnailInput.files.length === 0) {
                return new Promise((resolve, reject) => {
                    html2canvas(document.getElementById('map'), {
                        useCORS: true
                    }).then(function (canvas) {
                        var imgData = canvas.toDataURL('image/png');
                        $('#thumbnail-preview').attr('src', imgData);

                        canvas.toBlob(function (blob) {
                            const file = new File([blob], "thumbnail.png", { type: "image/png" });
                            const dataTransfer = new DataTransfer();
                            dataTransfer.items.add(file);
                            thumbnailInput.files = dataTransfer.files;

                            console.log('지도 캡처 완료 및 썸네일로 설정');
                            resolve();
                        });
                    }).catch(function (error) {
                        console.error('지도를 캡처하는 동안 오류가 발생했습니다:', error);
                        reject(error);
                    });
                });
            } else {
                return Promise.resolve(); // 이미 썸네일이 있는 경우
            }
        }

    // 출발지, 목적지 변경 시 지도 업데이트
    $('#departure, #destination').on('change', updateMap);

    // 경유지 변경 시 지도 업데이트
    $(document).on('change', '.waypoint-group input', updateMap);
});
