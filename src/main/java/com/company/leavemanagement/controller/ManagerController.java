package com.company.leavemanagement.controller;

import com.company.leavemanagement.dto.LeaveResponse;
import com.company.leavemanagement.dto.LeaveReviewRequest;
import com.company.leavemanagement.enums.LeaveStatus;
import com.company.leavemanagement.security.UserPrincipal;
import com.company.leavemanagement.service.LeaveService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints restricted to users with ROLE_MANAGER (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Tag(name = "Manager", description = "Review, approve or reject employee leave requests")
public class ManagerController {

    private final LeaveService leaveService;

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveResponse>> getAllLeaveRequests(
            @RequestParam(required = false) LeaveStatus status) {
        return ResponseEntity.ok(leaveService.getAllLeaveRequests(status));
    }

    @PatchMapping("/leaves/{id}/review")
    public ResponseEntity<LeaveResponse> reviewLeaveRequest(@AuthenticationPrincipal UserPrincipal principal,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody LeaveReviewRequest request) {
        return ResponseEntity.ok(leaveService.reviewLeaveRequest(id, principal.getId(), request));
    }
}
