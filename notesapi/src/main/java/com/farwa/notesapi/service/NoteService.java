package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.NoteRequestDto;
import com.farwa.notesapi.dto.NoteResponseDto;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.repository.CategoryRepository;
import com.farwa.notesapi.repository.NoteRepository;
import com.farwa.notesapi.repository.TagRepository;
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
                .orElseThrow();

        List<Tag> tags = tagRepository
                .findAllById(requestDto.getTagIds());

        Note note = new Note();

        note.setTitle(requestDto.getTitle());
        note.setContent(requestDto.getContent());
        note.setCategory(category);
        note.setTags(tags);
        Note savedNote = noteRepository.save(note);

        NoteResponseDto responseDto = new NoteResponseDto();

        responseDto.setId(savedNote.getId());
        responseDto.setTitle(savedNote.getTitle());
        responseDto.setContent(savedNote.getContent());
        responseDto.setCreatedAt(savedNote.getCreatedAt());
        responseDto.setUpdatedAt(savedNote.getUpdatedAt());
        responseDto.setCategoryId(savedNote.getCategory().getId());

        responseDto.setTagIds(
                savedNote.getTags()
                        .stream()
                        .map(Tag::getId)
                        .toList()
        );

        return responseDto;
    }
}

