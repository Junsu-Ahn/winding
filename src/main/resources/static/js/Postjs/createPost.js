var geocoder = new kakao.maps.services.Geocoder();

function searchLocation(target) {
    var address = document.getElementById(target).value;
    geocoder.addressSearch(address, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
            var coords = new kakao.maps.LatLng(result[0].y, result[0].x);
            document.getElementById(target + 'Lat').value = result[0].y;
            document.getElementById(target + 'Lng').value = result[0].x;
            displayMarker(coords);
        } else {
            alert('주소를 찾을 수 없습니다.');
        }
    });
}

function displayMarker(coords) {
    var mapContainer = document.getElementById('map'), // 지도를 표시할 div
        mapOption = {
            center: coords, // 지도의 중심좌표
            level: 3 // 지도의 확대 레벨
        };

    var map = new kakao.maps.Map(mapContainer, mapOption); // 지도를 생성합니다

    var marker = new kakao.maps.Marker({
        map: map,
        position: coords
    });
}

document.getElementById('departure').addEventListener('blur', function() {
    searchLocation('departure');
});

document.getElementById('destination').addEventListener('blur', function() {
    searchLocation('destination');
});

document.getElementById('waypoint1').addEventListener('blur', function() {
    searchLocation('waypoint1');
});

document.getElementById('waypoint2').addEventListener('blur', function() {
    searchLocation('waypoint2');
});

document.getElementById('waypoint3').addEventListener('blur', function() {
    searchLocation('waypoint3');
});
