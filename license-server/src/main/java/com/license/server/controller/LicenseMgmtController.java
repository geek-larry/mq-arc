package com.license.server.controller;

import com.license.common.payload.kickout.FlexnetUserKickoutPayload;
import com.license.common.payload.kickout.LmxUserKickoutPayload;
import com.license.common.payload.kickout.SentinelUserKickoutPayload;
import com.license.common.payload.renew.FlexnetUserRenewPayload;
import com.license.common.payload.renew.LmxUserRenewPayload;
import com.license.common.payload.renew.SentinelUserRenewPayload;
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

    // ==================== 用户踢出接口 ====================

    @PostMapping("/FLEXNET/{hostname}/users/kickout")
    public ResponseEntity<Void> kickoutFlexnetUser(
            @PathVariable String hostname,
            @RequestBody FlexnetUserKickoutPayload payload) {
        log.info("API: Kickout Flexnet user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.kickoutUser("flexnet", hostname, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/SENTINEL/{hostname}/users/kickout")
    public ResponseEntity<Void> kickoutSentinelUser(
            @PathVariable String hostname,
            @RequestBody SentinelUserKickoutPayload payload) {
        log.info("API: Kickout Sentinel user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.kickoutUser("sentinel", hostname, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/LMX/{hostname}/users/kickout")
    public ResponseEntity<Void> kickoutLmxUser(
            @PathVariable String hostname,
            @RequestBody LmxUserKickoutPayload payload) {
        log.info("API: Kickout LMX user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.kickoutUser("lmx", hostname, payload);
        return ResponseEntity.ok().build();
    }

    // ==================== 用户续期接口 ====================

    @PostMapping("/FLEXNET/{hostname}/users/renew")
    public ResponseEntity<Void> renewFlexnetUser(
            @PathVariable String hostname,
            @RequestBody FlexnetUserRenewPayload payload) {
        log.info("API: Renew Flexnet user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.renewUser("flexnet", hostname, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/SENTINEL/{hostname}/users/renew")
    public ResponseEntity<Void> renewSentinelUser(
            @PathVariable String hostname,
            @RequestBody SentinelUserRenewPayload payload) {
        log.info("API: Renew Sentinel user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.renewUser("sentinel", hostname, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/LMX/{hostname}/users/renew")
    public ResponseEntity<Void> renewLmxUser(
            @PathVariable String hostname,
            @RequestBody LmxUserRenewPayload payload) {
        log.info("API: Renew LMX user {} on {}", payload.getUsername(), hostname);
        licenseMgmtService.renewUser("lmx", hostname, payload);
        return ResponseEntity.ok().build();
    }
}
