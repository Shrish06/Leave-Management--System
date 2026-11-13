package com.company.leavemanagement.service;

import com.company.leavemanagement.dto.LeaveApplyRequest;
import com.company.leavemanagement.dto.LeaveBalanceResponse;
import com.company.leavemanagement.dto.LeaveResponse;
import com.company.leavemanagement.dto.LeaveReviewRequest;
import com.company.leavemanagement.enums.LeaveStatus;

import java.util.List;

public interface LeaveService {

    LeaveResponse applyForLeave(Long employeeId, LeaveApplyRequest request);

    List<LeaveResponse> getLeaveHistory(Long employeeId);

    LeaveBalanceResponse getLeaveBalance(Long employeeId);

    List<LeaveResponse> getAllLeaveRequests(LeaveStatus statusFilter);

    LeaveResponse reviewLeaveRequest(Long leaveRequestId, Long managerId, LeaveReviewRequest request);

    LeaveResponse cancelLeaveRequest(Long employeeId, Long leaveRequestId);
}
