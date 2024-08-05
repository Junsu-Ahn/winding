package com.example.demo.map.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional
public class MapService {
    // 카카오맵 URL을 생성하는 메서드
    public String getMapUrl(String address) {
        // 주소를 URL 인코딩합니다.
        String encodedAddress = UriComponentsBuilder
                .fromUriString("https://map.kakao.com/")
                .queryParam("q", address)
                .encode()
                .toUriString();

        // 생성된 URL을 반환합니다.
        return encodedAddress;
    }
}
