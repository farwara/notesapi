package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.ReminderRequestDto;
import com.farwa.notesapi.dto.ReminderResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.model.Reminder;
import com.farwa.notesapi.repository.NoteRepository;
import com.farwa.notesapi.repository.ReminderRepository;
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

    // CREATE
    public ReminderResponseDto createReminder(ReminderRequestDto requestDto) {

        Note note = noteRepository.findById(requestDto.getNoteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found"));

        Reminder reminder = new Reminder();
        reminder.setDate(requestDto.getDate());
        reminder.setTime(requestDto.getTime());
        reminder.setNote(note);

        Reminder savedReminder = reminderRepository.save(reminder);

        return mapToResponse(savedReminder);
    }

    // GET ONE
    public ReminderResponseDto getReminderById(Long id) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reminder not found"));

        return mapToResponse(reminder);
    }

    // GET ALL
    public List<ReminderResponseDto> getAllReminders() {

        return reminderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // UPDATE
    public ReminderResponseDto updateReminder(
            Long id,
            ReminderRequestDto requestDto) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reminder not found"));

        Note note = noteRepository.findById(requestDto.getNoteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found"));

        reminder.setDate(requestDto.getDate());
        reminder.setTime(requestDto.getTime());
        reminder.setNote(note);

        Reminder updatedReminder = reminderRepository.save(reminder);

        return mapToResponse(updatedReminder);
    }

    // DELETE
    public void deleteReminder(Long id) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        Note note = reminder.getNote();

        if (note != null) {
            note.setReminder(null);
        }

        reminderRepository.delete(reminder);
    }

    // ENTITY -> RESPONSE DTO
    private ReminderResponseDto mapToResponse(Reminder reminder) {

        ReminderResponseDto responseDto = new ReminderResponseDto();

        responseDto.setId(reminder.getId());
        responseDto.setDate(reminder.getDate());
        responseDto.setTime(reminder.getTime());
        responseDto.setNoteId(reminder.getNote().getId());

        return responseDto;
    }
}