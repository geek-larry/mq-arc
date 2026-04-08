package com.license.server.controller;

import com.license.common.enums.SoftwareType;
import com.license.common.payload.kickout.FlexnetUserKickoutPayload;
import com.license.common.payload.kickout.LmxUserKickoutPayload;
import com.license.common.payload.kickout.SentinelUserKickoutPayload;
import com.license.server.service.LicenseMgmtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 许可管理 REST API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseMgmtController {

    private final LicenseMgmtService licenseMgmtService;

    @PostMapping("/FLEXNET/{hostname}/users/kickout")
    public ResponseEntity<Void> kickoutFlexnetUser(
            @PathVariable String hostname,
            @RequestBody FlexnetUserKickoutPayload payload) {
        log.info("API: Kickout Flexnet user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.kickoutUser(SoftwareType.FLEXNET, hostname, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/SENTINEL/{hostname}/users/kickout")
    public ResponseEntity<Void> kickoutSentinelUser(
            @PathVariable String hostname,
            @RequestBody SentinelUserKickoutPayload payload) {
        log.info("API: Kickout Sentinel user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.kickoutUser(SoftwareType.SENTINEL, hostname, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/LMX/{hostname}/users/kickout")
    public ResponseEntity<Void> kickoutLmxUser(
            @PathVariable String hostname,
            @RequestBody LmxUserKickoutPayload payload) {
        log.info("API: Kickout LMX user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.kickoutUser(SoftwareType.LMX, hostname, payload);
        return ResponseEntity.ok().build();
    }
}
