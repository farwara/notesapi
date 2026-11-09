
package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.NoteRequestDto;
import com.farwa.notesapi.dto.NoteResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.AppUser;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.model.Tag;
import com.farwa.notesapi.repository.AppUserRepository;
import com.farwa.notesapi.repository.CategoryRepository;
import com.farwa.notesapi.repository.NoteRepository;
import com.farwa.notesapi.repository.TagRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final AppUserRepository appUserRepository;

    public NoteService(
            NoteRepository noteRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            AppUserRepository appUserRepository
    ) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.appUserRepository = appUserRepository;
    }

    // Get the authenticated Keycloak user
    private JwtAuthenticationToken getAuthentication() {

        if (!(SecurityContextHolder.getContext()
                .getAuthentication() instanceof JwtAuthenticationToken authentication)) {

            throw new AccessDeniedException(
                    "You must be logged in"
            );
        }

        return authentication;
    }

    // Check whether the logged-in user is an ADMIN
    private boolean isAdmin() {

        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );
    }

    // Get the current user from PostgreSQL
    private AppUser getCurrentUser() {

        String keycloakId = getAuthentication()
                .getToken()
                .getSubject();

        return appUserRepository
                .findByKeycloakId(keycloakId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Logged-in user is not linked to a database account"
                        )
                );
    }

    // Check if the logged-in user owns a note
    // ADMIN users are allowed to manage all notes
    private void checkOwnership(Note note) {

        if (isAdmin()) {
            return;
        }

        String keycloakId = getAuthentication()
                .getToken()
                .getSubject();

        if (note.getUser() == null ||
                !keycloakId.equals(note.getUser().getKeycloakId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this note"
            );
        }
    }

    // CREATE NOTE
    public NoteResponseDto createNote(NoteRequestDto requestDto) {

        AppUser currentUser = getCurrentUser();

        Category category = categoryRepository
                .findById(requestDto.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        )
                );

        List<Tag> tags = tagRepository
                .findAllById(requestDto.getTagIds());

        if (tags.size() != requestDto.getTagIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more tags not found"
            );
        }

        Note note = new Note();

        note.setTitle(requestDto.getTitle());
        note.setContent(requestDto.getContent());
        note.setCategory(category);
        note.setTags(tags);
        note.setUser(currentUser);

        Note savedNote = noteRepository.save(note);

        return mapToResponse(savedNote);
    }

    // GET NOTE BY ID
    public NoteResponseDto getNoteById(Long id) {

        Note note = noteRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Note not found"
                        )
                );

        checkOwnership(note);

        return mapToResponse(note);
    }

    // GET ALL NOTES
    public List<NoteResponseDto> getAllNotes() {

        List<Note> notes = noteRepository.findAll();

        if (isAdmin()) {
            return notes.stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        String keycloakId = getAuthentication()
                .getToken()
                .getSubject();

        return notes.stream()
                .filter(note ->
                        note.getUser() != null &&
                                keycloakId.equals(
                                        note.getUser().getKeycloakId()
                                )
                )
                .map(this::mapToResponse)
                .toList();
    }

    // UPDATE NOTE
    public NoteResponseDto updateNote(
            Long id,
            NoteRequestDto requestDto
    ) {

        Note note = noteRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Note not found"
                        )
                );

        checkOwnership(note);

        Category category = categoryRepository
                .findById(requestDto.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        )
                );

        List<Tag> tags = tagRepository
                .findAllById(requestDto.getTagIds());

        if (tags.size() != requestDto.getTagIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more tags not found"
            );
        }

        note.setTitle(requestDto.getTitle());
        note.setContent(requestDto.getContent());
        note.setCategory(category);
        note.setTags(tags);

        // Do not change the note owner during an update
        Note updatedNote = noteRepository.save(note);

        return mapToResponse(updatedNote);
    }

    // DELETE NOTE
    public void deleteNote(Long id) {

        Note note = noteRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Note not found"
                        )
                );

        checkOwnership(note);

        noteRepository.delete(note);
    }

    // CONVERT NOTE TO RESPONSE DTO
    private NoteResponseDto mapToResponse(Note note) {

        NoteResponseDto responseDto = new NoteResponseDto();

        responseDto.setId(note.getId());
        responseDto.setTitle(note.getTitle());
        responseDto.setContent(note.getContent());
        responseDto.setCreatedAt(note.getCreatedAt());
        responseDto.setUpdatedAt(note.getUpdatedAt());

        if (note.getCategory() != null) {
            responseDto.setCategoryId(
                    note.getCategory().getId()
            );
        }

        responseDto.setTagIds(
                note.getTags() == null
                        ? List.of()
                        : note.getTags()
                        .stream()
                        .map(Tag::getId)
                        .toList()
        );

        return responseDto;
    }
}
