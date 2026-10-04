package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.ReminderRequestDto;
import com.farwa.notesapi.dto.ReminderResponseDto;
import com.farwa.notesapi.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // CREATE
    @PostMapping
    public ReminderResponseDto createReminder(
            @Valid @RequestBody ReminderRequestDto requestDto) {

        return reminderService.createReminder(requestDto);
    }

    // GET ALL
    @GetMapping
    public List<ReminderResponseDto> getAllReminders() {

        return reminderService.getAllReminders();
    }

    // GET ONE
    @GetMapping("/{id}")
    public ReminderResponseDto getReminderById(@PathVariable Long id) {

        return reminderService.getReminderById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ReminderResponseDto updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderRequestDto requestDto) {

        return reminderService.updateReminder(id, requestDto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteReminder(@PathVariable Long id) {

        reminderService.deleteReminder(id);
    }
}