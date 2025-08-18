package com.example.Leave_management_system.Repository;




import com.example.Leave_management_system.model.LeaveRequest;
import com.example.Leave_management_system.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Collection;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // Overlap check: dates clash with any PENDING/APPROVED leave
    boolean existsByEmployee_IdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            Long employeeId, Collection<LeaveStatus> statuses, LocalDate start, LocalDate end);

    // Sum APPROVED days for the calendar year of startDate
    @Query("""
    select coalesce(sum(l.days),0) 
    from LeaveRequest l
    where l.employee.id = :employeeId
      and l.status = com.example.Leave_management_system.model.LeaveStatus.APPROVED
      and year(l.startDate) = year(current_date)
  """)
    int sumApprovedDaysInCurrentYear(Long employeeId);

    // Portable (works on H2/Postgres) – compute between Jan 1 and Dec 31 explicitly
    @Query("""
    select coalesce(sum(l.days),0)
    from LeaveRequest l
    where l.employee.id = :employeeId
      and l.status = com.example.Leave_management_system.model.LeaveStatus.APPROVED
      and l.startDate >= :yearStart and l.startDate <= :yearEnd
  """)
    int sumApprovedDaysBetween(Long employeeId, LocalDate yearStart, LocalDate yearEnd);
}
