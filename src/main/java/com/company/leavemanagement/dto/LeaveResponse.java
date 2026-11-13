package com.company.leavemanagement.dto;

import com.company.leavemanagement.enums.LeaveStatus;
import com.company.leavemanagement.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String department;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    private String reason;
    private LeaveStatus status;
    private String reviewedByName;
    private String managerComment;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
}
