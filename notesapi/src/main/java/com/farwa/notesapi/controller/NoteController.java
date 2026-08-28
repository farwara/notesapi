package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.NoteRequestDto;
import com.farwa.notesapi.dto.NoteResponseDto;
import com.farwa.notesapi.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
@RestController
@RequestMapping ("api/notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }
    @PostMapping
    public NoteResponseDto createNote(
            @Valid @RequestBody NoteRequestDto requestDto
    ) {
        return noteService.createNote(requestDto);
    }
    @GetMapping
    public List<NoteResponseDto> getAllNotes() {
        return noteService.getAllNotes();
    }
    @GetMapping("/{id}")
    public NoteResponseDto getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }
    @PutMapping("/{id}")
    public NoteResponseDto updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequestDto requestDto
    ) {
        return noteService.updateNote(id, requestDto);
    }
    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
    }
}

