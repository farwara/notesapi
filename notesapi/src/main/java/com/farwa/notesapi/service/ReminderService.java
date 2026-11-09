
package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.ReminderRequestDto;
import com.farwa.notesapi.dto.ReminderResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.model.Reminder;
import com.farwa.notesapi.repository.NoteRepository;
import com.farwa.notesapi.repository.ReminderRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;

    public ReminderService(ReminderRepository reminderRepository,
                           NoteRepository noteRepository) {
        this.reminderRepository = reminderRepository;
        this.noteRepository = noteRepository;
    }

    // CREATE REMINDER
    public ReminderResponseDto createReminder(ReminderRequestDto requestDto) {

        Note note = noteRepository.findById(requestDto.getNoteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found"));

        checkNoteOwnership(note);

        Reminder reminder = new Reminder();
        reminder.setDate(requestDto.getDate());
        reminder.setTime(requestDto.getTime());
        reminder.setNote(note);

        Reminder savedReminder = reminderRepository.save(reminder);

        return mapToResponse(savedReminder);
    }

    // GET ONE REMINDER
    public ReminderResponseDto getReminderById(Long id) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reminder not found"));

        checkNoteOwnership(reminder.getNote());

        return mapToResponse(reminder);
    }

    // GET ALL REMINDERS
    public List<ReminderResponseDto> getAllReminders() {

        List<Reminder> reminders = reminderRepository.findAll();

        if (isAdmin()) {
            return reminders.stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        String keycloakId = getAuthentication()
                .getToken()
                .getSubject();

        return reminders.stream()
                .filter(reminder ->
                        reminder.getNote() != null
                                && reminder.getNote().getUser() != null
                                && keycloakId.equals(
                                reminder.getNote()
                                        .getUser()
                                        .getKeycloakId()))
                .map(this::mapToResponse)
                .toList();
    }

    // UPDATE REMINDER
    public ReminderResponseDto updateReminder(
            Long id,
            ReminderRequestDto requestDto) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reminder not found"));

        // Check ownership of the existing reminder
        checkNoteOwnership(reminder.getNote());

        Note note = noteRepository.findById(requestDto.getNoteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found"));

        // Check ownership of the destination note too
        checkNoteOwnership(note);

        reminder.setDate(requestDto.getDate());
        reminder.setTime(requestDto.getTime());
        reminder.setNote(note);

        Reminder updatedReminder = reminderRepository.save(reminder);

        return mapToResponse(updatedReminder);
    }

    // DELETE REMINDER
    public void deleteReminder(Long id) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reminder not found"));

        checkNoteOwnership(reminder.getNote());

        Note note = reminder.getNote();

        if (note != null) {
            note.setReminder(null);
        }

        reminderRepository.delete(reminder);
    }

    // GET CURRENT KEYCLOAK AUTHENTICATION
    private JwtAuthenticationToken getAuthentication() {

        if (!(SecurityContextHolder.getContext()
                .getAuthentication()
                instanceof JwtAuthenticationToken authentication)) {

            throw new AccessDeniedException("You must be logged in");
        }

        return authentication;
    }

    // CHECK ADMIN ROLE
    private boolean isAdmin() {

        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));
    }

    // CHECK NOTE OWNERSHIP
    private void checkNoteOwnership(Note note) {

        if (isAdmin()) {
            return;
        }

        String keycloakId = getAuthentication()
                .getToken()
                .getSubject();

        if (note == null
                || note.getUser() == null
                || !keycloakId.equals(
                note.getUser().getKeycloakId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this reminder");
        }
    }

    // CONVERT REMINDER ENTITY TO RESPONSE DTO
    private ReminderResponseDto mapToResponse(Reminder reminder) {

        ReminderResponseDto responseDto = new ReminderResponseDto();

        responseDto.setId(reminder.getId());
        responseDto.setDate(reminder.getDate());
        responseDto.setTime(reminder.getTime());
        responseDto.setNoteId(reminder.getNote().getId());

        return responseDto;
    }
}
