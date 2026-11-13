package com.company.leavemanagement.controller;

import com.company.leavemanagement.dto.LeaveApplyRequest;
import com.company.leavemanagement.dto.LeaveBalanceResponse;
import com.company.leavemanagement.dto.LeaveResponse;
import com.company.leavemanagement.entity.User;
import com.company.leavemanagement.security.UserPrincipal;
import com.company.leavemanagement.service.LeaveService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints available to a logged-in employee (also accessible to managers,
 * since a manager is an employee of the organization too and may want to
 * apply for their own leave).
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Apply for leave, view leave history and balance")
public class EmployeeController {

    private final LeaveService leaveService;

    /**
     * Simple "dashboard" endpoint: returns the logged-in user's own profile
     * details (name, department, role) as displayed on their dashboard header.
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(principal.getUser());
    }

    @PostMapping("/leaves")
    public ResponseEntity<LeaveResponse> applyForLeave(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody LeaveApplyRequest request) {
        LeaveResponse response = leaveService.applyForLeave(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveResponse>> getMyLeaveHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(leaveService.getLeaveHistory(principal.getId()));
    }

    @GetMapping("/leaves/balance")
    public ResponseEntity<LeaveBalanceResponse> getMyLeaveBalance(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(principal.getId()));
    }

    @PatchMapping("/leaves/{id}/cancel")
    public ResponseEntity<LeaveResponse> cancelLeave(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable Long id) {
        return ResponseEntity.ok(leaveService.cancelLeaveRequest(principal.getId(), id));
    }
}
