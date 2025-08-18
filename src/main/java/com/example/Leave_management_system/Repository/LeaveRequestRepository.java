// src/main/java/com/example/Leave_management_system/Repository/LeaveRequestRepository.java
package com.example.Leave_management_system.Repository;

import com.example.Leave_management_system.model.LeaveRequest;
import com.example.Leave_management_system.model.LeaveStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.Collection;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    boolean existsByEmployee_IdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            Long employeeId, Collection<LeaveStatus> statuses, LocalDate start, LocalDate end);

    @Query("""
      select coalesce(sum(l.days),0)
      from LeaveRequest l
      where l.employee.id = :employeeId
        and l.status = com.example.Leave_management_system.model.LeaveStatus.APPROVED
        and l.startDate >= :yearStart and l.startDate <= :yearEnd
    """)
    int sumApprovedDaysBetween(Long employeeId, LocalDate yearStart, LocalDate yearEnd);

    // ---- Listing helpers (fetch employee+approver to avoid lazy serialization) ----
    @EntityGraph(attributePaths = {"employee", "approver"})
    Page<LeaveRequest> findAllByStatusOrderByCreatedAtDesc(LeaveStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "approver"})
    Page<LeaveRequest> findAllByEmployee_IdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "approver"})
    Page<LeaveRequest> findAllByEmployee_IdAndStatusOrderByCreatedAtDesc(Long employeeId, LeaveStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"employee", "approver"})
    Page<LeaveRequest> findAll(Pageable pageable);
}
