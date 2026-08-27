package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.NoteRequestDto;
import com.farwa.notesapi.dto.NoteResponseDto;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.repository.CategoryRepository;
import com.farwa.notesapi.repository.NoteRepository;
import com.farwa.notesapi.repository.TagRepository;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.model.Tag;
import java.util.List;
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public NoteService(
            NoteRepository noteRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository
    ) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    public NoteResponseDto createNote(NoteRequestDto requestDto) {

        Category category = categoryRepository
                .findById(requestDto.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        List<Tag> tags = tagRepository
                .findAllById(requestDto.getTagIds());
        if (tags.size() != requestDto.getTagIds().size()) {
            throw new ResourceNotFoundException("One or more tags not found");
        }
        Note note = new Note();

        note.setTitle(requestDto.getTitle());
        note.setContent(requestDto.getContent());
        note.setCategory(category);
        note.setTags(tags);
        Note savedNote = noteRepository.save(note);
        return mapToResponse(savedNote);
    }

    public NoteResponseDto getNoteById(Long id) {

        Note note = noteRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found")
                );

        return mapToResponse(note);
    }
    public List<NoteResponseDto> getAllNotes() {

        return noteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public NoteResponseDto updateNote(Long id, NoteRequestDto requestDto) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found")
                );

        Category category = categoryRepository
                .findById(requestDto.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        List<Tag> tags = tagRepository
                .findAllById(requestDto.getTagIds());

        if (tags.size() != requestDto.getTagIds().size()) {
            throw new ResourceNotFoundException("One or more tags not found");
        }

        note.setTitle(requestDto.getTitle());
        note.setContent(requestDto.getContent());
        note.setCategory(category);
        note.setTags(tags);

        Note updatedNote = noteRepository.save(note);

        return mapToResponse(updatedNote);
    }
    public void deleteNote(Long id) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found")
                );

        noteRepository.delete(note);
    }
    private NoteResponseDto mapToResponse(Note note) {

        NoteResponseDto responseDto = new NoteResponseDto();

        responseDto.setId(note.getId());
        responseDto.setTitle(note.getTitle());
        responseDto.setContent(note.getContent());
        responseDto.setCreatedAt(note.getCreatedAt());
        responseDto.setUpdatedAt(note.getUpdatedAt());
        responseDto.setCategoryId(note.getCategory().getId());

        responseDto.setTagIds(
                note.getTags()
                        .stream()
                        .map(Tag::getId)
                        .toList()
        );

        return responseDto;
    }

}


