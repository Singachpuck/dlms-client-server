package com.dochkas.mandjetDlmsWeb.controller;

import com.dochkas.mandjetDlmsWeb.service.dlms.MandjetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mandjet")
@RequiredArgsConstructor
public class MandjetRestController {

    private final MandjetService mandjetService;

    @GetMapping("/data")
    ResponseEntity<?> sensorValues() {
        return ResponseEntity.ok(mandjetService.getMandgetData());
    }
}
