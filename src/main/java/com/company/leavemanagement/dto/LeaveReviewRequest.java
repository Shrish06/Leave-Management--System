package com.company.leavemanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveReviewRequest {

    /** true = approve, false = reject */
    @NotNull(message = "Decision (approve/reject) is required")
    private Boolean approve;

    private String comment;
}
