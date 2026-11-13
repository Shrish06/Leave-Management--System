package com.company.leavemanagement.enums;

/**
 * Represents the two roles supported by the system.
 * Spring Security expects authorities prefixed with "ROLE_",
 * this is handled in the UserDetails implementation.
 */
public enum Role {
    EMPLOYEE,
    MANAGER
}
