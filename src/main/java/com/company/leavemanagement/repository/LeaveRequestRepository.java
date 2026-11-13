package com.company.leavemanagement.repository;

import com.company.leavemanagement.entity.LeaveRequest;
import com.company.leavemanagement.entity.User;
import com.company.leavemanagement.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeOrderByAppliedAtDesc(User employee);

    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByAppliedAtDesc(LeaveStatus status);

    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
}
