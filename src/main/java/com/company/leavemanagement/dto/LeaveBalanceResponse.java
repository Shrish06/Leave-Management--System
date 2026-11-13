package com.company.leavemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceResponse {
    private Long employeeId;
    private String employeeName;
    private Integer totalLeaveBalance;
    private Integer usedLeaveBalance;
    private Integer remainingLeaveBalance;
    private Long pendingRequestsCount;
}
