package com.example.demo.map.controller;

import com.example.demo.map.service.NavigationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NavigationController {

    private final NavigationService navigationService;

    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @GetMapping("/navigation-route")
    public ResponseEntity<String> getNavigationRoute(@RequestParam("origin") String origin,
                                                     @RequestParam("destination") String destination,
                                                     @RequestParam(value = "waypoints", required = false) String waypoints) {
        try {
            String route = navigationService.getNavigationRoute(origin, destination, waypoints);
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
