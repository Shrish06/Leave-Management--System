package com.company.leavemanagement.service.impl;

import com.company.leavemanagement.dto.LeaveApplyRequest;
import com.company.leavemanagement.dto.LeaveBalanceResponse;
import com.company.leavemanagement.dto.LeaveResponse;
import com.company.leavemanagement.dto.LeaveReviewRequest;
import com.company.leavemanagement.entity.LeaveRequest;
import com.company.leavemanagement.entity.User;
import com.company.leavemanagement.enums.LeaveStatus;
import com.company.leavemanagement.exception.InvalidLeaveRequestException;
import com.company.leavemanagement.exception.ResourceNotFoundException;
import com.company.leavemanagement.repository.LeaveRequestRepository;
import com.company.leavemanagement.repository.UserRepository;
import com.company.leavemanagement.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LeaveResponse applyForLeave(Long employeeId, LeaveApplyRequest request) {
        User employee = getUserOrThrow(employeeId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidLeaveRequestException("End date cannot be before start date");
        }

        int numberOfDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        if (numberOfDays > employee.getRemainingLeaveBalance()) {
            throw new InvalidLeaveRequestException(
                    "Insufficient leave balance. Requested: " + numberOfDays +
                            ", Available: " + employee.getRemainingLeaveBalance());
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .numberOfDays(numberOfDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    public List<LeaveResponse> getLeaveHistory(Long employeeId) {
        getUserOrThrow(employeeId); // validate employee exists
        return leaveRequestRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LeaveBalanceResponse getLeaveBalance(Long employeeId) {
        User employee = getUserOrThrow(employeeId);
        long pendingCount = leaveRequestRepository.countByEmployeeIdAndStatus(employeeId, LeaveStatus.PENDING);

        return LeaveBalanceResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .totalLeaveBalance(employee.getTotalLeaveBalance())
                .usedLeaveBalance(employee.getUsedLeaveBalance())
                .remainingLeaveBalance(employee.getRemainingLeaveBalance())
                .pendingRequestsCount(pendingCount)
                .build();
    }

    @Override
    public List<LeaveResponse> getAllLeaveRequests(LeaveStatus statusFilter) {
        List<LeaveRequest> requests = (statusFilter == null)
                ? leaveRequestRepository.findAllByOrderByAppliedAtDesc()
                : leaveRequestRepository.findByStatusOrderByAppliedAtDesc(statusFilter);

        return requests.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public LeaveResponse reviewLeaveRequest(Long leaveRequestId, Long managerId, LeaveReviewRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveRequestException("This leave request has already been reviewed");
        }

        User manager = getUserOrThrow(managerId);

        if (Boolean.TRUE.equals(request.getApprove())) {
            User employee = leaveRequest.getEmployee();
            if (leaveRequest.getNumberOfDays() > employee.getRemainingLeaveBalance()) {
                throw new InvalidLeaveRequestException("Employee no longer has sufficient leave balance");
            }
            employee.setUsedLeaveBalance(employee.getUsedLeaveBalance() + leaveRequest.getNumberOfDays());
            userRepository.save(employee);
            leaveRequest.setStatus(LeaveStatus.APPROVED);
        } else {
            leaveRequest.setStatus(LeaveStatus.REJECTED);
        }

        leaveRequest.setReviewedBy(manager);
        leaveRequest.setManagerComment(request.getComment());
        leaveRequest.setReviewedAt(java.time.LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeaveRequest(Long employeeId, Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveRequestId));

        if (!leaveRequest.getEmployee().getId().equals(employeeId)) {
            throw new InvalidLeaveRequestException("You can only cancel your own leave requests");
        }

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveRequestException("Only pending leave requests can be cancelled");
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    // ---------- helpers ----------

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private LeaveResponse toResponse(LeaveRequest lr) {
        return LeaveResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(lr.getEmployee().getFullName())
                .department(lr.getEmployee().getDepartment())
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .numberOfDays(lr.getNumberOfDays())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .reviewedByName(lr.getReviewedBy() != null ? lr.getReviewedBy().getFullName() : null)
                .managerComment(lr.getManagerComment())
                .appliedAt(lr.getAppliedAt())
                .reviewedAt(lr.getReviewedAt())
                .build();
    }
}
