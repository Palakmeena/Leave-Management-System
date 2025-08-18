package com.example.Leave_management_system.service;


import com.example.Leave_management_system.Repository.LeaveRequestRepository;
import com.example.Leave_management_system.dto.ApplyLeaveRequest;
import com.example.Leave_management_system.dto.BalanceResponse;

import com.example.Leave_management_system.dto.DecisionRequest;
import com.example.Leave_management_system.exception.BadRequestException;
import com.example.Leave_management_system.exception.ConflictException;
import com.example.Leave_management_system.exception.ForbiddenException;
import com.example.Leave_management_system.exception.NotFoundException;
import com.example.Leave_management_system.model.LeaveRequest;
import com.example.Leave_management_system.model.LeaveStatus;
import com.example.Leave_management_system.model.LeaveType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service @RequiredArgsConstructor
public class LeaveService {

    private final EmployeeService employeeService;
    private final LeaveRequestRepository leaveRepo;

    /** Year window helper */
    private static LocalDate startOfYear(LocalDate d){ return LocalDate.of(d.getYear(),1,1); }
    private static LocalDate endOfYear(LocalDate d){ return LocalDate.of(d.getYear(),12,31); }

    /** Inclusive date-days: e.g. 20-22 = 3 */
    private static int inclusiveDays(LocalDate s, LocalDate e){
        long diff = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
        if (diff <= 0) throw new BadRequestException("endDate must be >= startDate");
        return (int) diff;
    }

    public LeaveRequest getById(Long id) {
        return leaveRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("leave not found: " + id));
    }

    @Transactional
    public Long apply(ApplyLeaveRequest req){
        var emp = employeeService.getOrThrow(req.getEmployeeId());

        var start = req.getStartDate();
        var end   = req.getEndDate();

        // Edge: invalid dates
        if (start == null || end == null) throw new BadRequestException("startDate/endDate required");
        if (end.isBefore(start)) throw new BadRequestException("endDate before startDate");

        // Edge: before joining date
        if (start.isBefore(emp.getJoiningDate()))
            throw new BadRequestException("cannot apply before joining date");

        // Edge: overlap with existing PENDING/APPROVED leaves
        var overlaps = leaveRepo.existsByEmployee_IdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                emp.getId(), java.util.List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED), start, end);
        if (overlaps) throw new ConflictException("overlapping leave exists");

        int reqDays = inclusiveDays(start, end);

        // Balance check (current year policy)
        var yStart = startOfYear(start);
        var yEnd   = endOfYear(start);
        int approved = leaveRepo.sumApprovedDaysBetween(emp.getId(), yStart, yEnd);
        int remaining = emp.getAnnualAllocationDays() - approved;
        if (reqDays > remaining)
            throw new BadRequestException("insufficient balance: remaining=" + remaining + ", requested=" + reqDays);

        var lr = LeaveRequest.builder()
                .employee(emp)
                .type(LeaveType.ANNUAL)
                .status(LeaveStatus.PENDING)
                .startDate(start)
                .endDate(end)
                .days(reqDays)
                .reason(req.getReason())
                .build();

        return leaveRepo.save(lr).getId();
    }

    @Transactional
    public void approve(Long leaveId, DecisionRequest req){
        var lr = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new NotFoundException("leave not found: " + leaveId));
        if (lr.getStatus() != LeaveStatus.PENDING)
            throw new ConflictException("only PENDING can be approved");

        var approver = employeeService.getOrThrow(req.getApproverId());
        if (!approver.isHr())
            throw new ForbiddenException("approver must be HR");
        if (approver.getId().equals(lr.getEmployee().getId()))
            throw new ForbiddenException("self-approval not allowed");

        // Balance re-check at decision time (in case state changed)
        var yStart = startOfYear(lr.getStartDate());
        var yEnd   = endOfYear(lr.getStartDate());
        int approved = leaveRepo.sumApprovedDaysBetween(lr.getEmployee().getId(), yStart, yEnd);
        int remaining = lr.getEmployee().getAnnualAllocationDays() - approved;
        if (lr.getDays() > remaining)
            throw new ConflictException("insufficient balance at approval time");

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApprover(approver);
        lr.setDecisionNote(req.getNote());
        // balance is implicit; we persist days for history
    }

    @Transactional
    public void reject(Long leaveId, DecisionRequest req){
        var lr = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new NotFoundException("leave not found: " + leaveId));
        if (lr.getStatus() != LeaveStatus.PENDING)
            throw new ConflictException("only PENDING can be rejected");

        var approver = employeeService.getOrThrow(req.getApproverId());
        if (!approver.isHr())
            throw new ForbiddenException("approver must be HR");

        lr.setStatus(LeaveStatus.REJECTED);
        lr.setApprover(approver);
        lr.setDecisionNote(req.getNote());
    }

    public BalanceResponse balance(Long employeeId){
        var emp = employeeService.getOrThrow(employeeId);
        var now = LocalDate.now();
        int approved = leaveRepo.sumApprovedDaysBetween(emp.getId(), startOfYear(now), endOfYear(now));
        int allocation = emp.getAnnualAllocationDays();
        int remaining = allocation - approved;
        return new BalanceResponse(emp.getId(), allocation, approved, remaining);
    }
}
