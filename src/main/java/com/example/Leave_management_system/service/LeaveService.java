// src/main/java/com/example/Leave_management_system/service/LeaveService.java
package com.example.Leave_management_system.service;

import com.example.Leave_management_system.Repository.LeaveRequestRepository;
import com.example.Leave_management_system.dto.*;
import com.example.Leave_management_system.exception.*;
import com.example.Leave_management_system.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class LeaveService {

    private final EmployeeService employeeService;
    private final LeaveRequestRepository leaveRepo;

    private static LocalDate startOfYear(LocalDate d){ return LocalDate.of(d.getYear(),1,1); }
    private static LocalDate endOfYear(LocalDate d){ return LocalDate.of(d.getYear(),12,31); }

    private static int inclusiveDays(LocalDate s, LocalDate e){
        long diff = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
        if (diff <= 0) throw new BadRequestException("endDate must be >= startDate");
        return (int) diff;
    }

    // ---------- DTO mapper (centralized) ----------
    private static LeaveDto toDto(LeaveRequest lr){
        var emp = lr.getEmployee();
        var appr = lr.getApprover();
        return LeaveDto.builder()
                .id(lr.getId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeName(emp != null ? emp.getName() : null)
                .department(emp != null ? emp.getDepartment() : null)
                .type(lr.getType() != null ? lr.getType().name() : null)
                .status(lr.getStatus() != null ? lr.getStatus().name() : null)
                .days(lr.getDays())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .reason(lr.getReason())
                .approverId(appr != null ? appr.getId() : null)
                .approverName(appr != null ? appr.getName() : null)
                .decisionNote(lr.getDecisionNote())
                .createdAt(lr.getCreatedAt())
                .updatedAt(lr.getUpdatedAt())
                .build();
    }

    // ---------- READ APIs (safe for serialization) ----------
    @Transactional(readOnly = true)
    public LeaveDto getDtoById(Long id) {
        var lr = leaveRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("leave not found: " + id));
        return toDto(lr);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveDto> listLeaves(String statusStr, Long employeeId, int page, int size) {
        if (page < 0) throw new BadRequestException("page must be >= 0");
        if (size <= 0 || size > 200) throw new BadRequestException("size must be in (1..200)");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LeaveStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = LeaveStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("invalid status: " + statusStr);
            }
        }

        Page<LeaveRequest> p;
        if (employeeId != null && status != null) {
            p = leaveRepo.findAllByEmployee_IdAndStatusOrderByCreatedAtDesc(employeeId, status, pageable);
        } else if (employeeId != null) {
            p = leaveRepo.findAllByEmployee_IdOrderByCreatedAtDesc(employeeId, pageable);
        } else if (status != null) {
            p = leaveRepo.findAllByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            p = leaveRepo.findAll(pageable);
        }

        List<LeaveDto> items = p.getContent().stream().map(LeaveService::toDto).toList();
        return PageResponse.<LeaveDto>builder()
                .items(items)
                .page(p.getNumber())
                .size(p.getSize())
                .total(p.getTotalElements())
                .build();
    }

    // ---------- Existing apply/approve/reject/balance logic (unchanged) ----------
    @Transactional
    public Long apply(ApplyLeaveRequest req){
        var emp = employeeService.getOrThrow(req.getEmployeeId());

        var start = req.getStartDate();
        var end   = req.getEndDate();

        if (start == null || end == null) throw new BadRequestException("startDate/endDate required");
        if (end.isBefore(start)) throw new BadRequestException("endDate before startDate");

        if (start.isBefore(emp.getJoiningDate()))
            throw new BadRequestException("cannot apply before joining date");

        var overlaps = leaveRepo.existsByEmployee_IdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                emp.getId(), java.util.List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED), start, end);
        if (overlaps) throw new ConflictException("overlapping leave exists");

        int reqDays = inclusiveDays(start, end);

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

        var yStart = startOfYear(lr.getStartDate());
        var yEnd   = endOfYear(lr.getStartDate());
        int approved = leaveRepo.sumApprovedDaysBetween(lr.getEmployee().getId(), yStart, yEnd);
        int remaining = lr.getEmployee().getAnnualAllocationDays() - approved;
        if (lr.getDays() > remaining)
            throw new ConflictException("insufficient balance at approval time");

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApprover(approver);
        lr.setDecisionNote(req.getNote());
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

    @Transactional(readOnly = true)
    public BalanceResponse balance(Long employeeId){
        var emp = employeeService.getOrThrow(employeeId);
        var now = LocalDate.now();
        int approved = leaveRepo.sumApprovedDaysBetween(emp.getId(), startOfYear(now), endOfYear(now));
        int allocation = emp.getAnnualAllocationDays();
        int remaining = allocation - approved;
        return new BalanceResponse(emp.getId(), allocation, approved, remaining);
    }
}
