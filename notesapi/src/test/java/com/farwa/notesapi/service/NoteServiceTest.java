
package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.NoteResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.AppUser;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.repository.AppUserRepository;
import com.farwa.notesapi.repository.CategoryRepository;
import com.farwa.notesapi.repository.NoteRepository;
import com.farwa.notesapi.repository.TagRepository;
import com.farwa.notesapi.dto.NoteRequestDto;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.model.Tag;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private NoteService noteService;

    // Clear authentication after every test
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Helper: simulate a logged-in Keycloak user
    private void loginAs(String keycloakId, String role) {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(new SimpleGrantedAuthority(role))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    // TEST 1: GET a note that does not exist
    @Test
    void getNoteById_whenNoteDoesNotExist_throwsException() {

        when(noteRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.getNoteById(999L)
        );
    }

    // TEST 2: DELETE a note that does not exist
    @Test
    void deleteNote_whenNoteDoesNotExist_throwsException() {

        when(noteRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.deleteNote(999L)
        );
    }

    // TEST 3: Another user cannot read a note
    @Test
    void getNoteById_whenUserIsNotOwner_throwsAccessDeniedException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("owner-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("different-user-id", "ROLE_USER");

        assertThrows(
                AccessDeniedException.class,
                () -> noteService.getNoteById(11L)
        );
    }

    // TEST 4: Another user cannot delete a note
    @Test
    void deleteNote_whenUserIsNotOwner_throwsAccessDeniedException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("owner-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("different-user-id", "ROLE_USER");

        assertThrows(
                AccessDeniedException.class,
                () -> noteService.deleteNote(11L)
        );

        verify(noteRepository, never()).delete(note);
    }

    // TEST 5: Owner can delete their own note
    @Test
    void deleteNote_whenUserIsOwner_deletesNote() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("farwa-keycloak-id", "ROLE_USER");

        noteService.deleteNote(11L);

        verify(noteRepository).delete(note);
    }

    // TEST 6: Owner can read their own note
    @Test
    void getNoteById_whenUserIsOwner_returnsNote() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setTitle("My First Note");
        note.setContent("Learning Spring Boot");
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("farwa-keycloak-id", "ROLE_USER");

        NoteResponseDto result = noteService.getNoteById(11L);

        assertEquals(11L, result.getId());
        assertEquals("My First Note", result.getTitle());
        assertEquals("Learning Spring Boot", result.getContent());
    }

    @Test
    void createNote_whenValidRequest_savesNote() {

        // Arrange: the logged-in user
        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        loginAs("farwa-keycloak-id", "ROLE_USER");

        when(appUserRepository.findByKeycloakId("farwa-keycloak-id"))
                .thenReturn(Optional.of(owner));

        // Create a category
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Create a tag
        Tag tag = new Tag();
        tag.setId(2L);

        when(tagRepository.findAllById(List.of(2L)))
                .thenReturn(List.of(tag));

        // Prepare the request
        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("My New Note");
        request.setContent("Learning unit testing");
        request.setCategoryId(1L);
        request.setTagIds(List.of(2L));

        // Pretend the database saves the note
        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> {
                    Note savedNote = invocation.getArgument(0);
                    savedNote.setId(20L);
                    return savedNote;
                });

        // Act
        NoteResponseDto result = noteService.createNote(request);

        // Assert
        assertEquals(20L, result.getId());
        assertEquals("My New Note", result.getTitle());
        assertEquals("Learning unit testing", result.getContent());
        assertEquals(1L, result.getCategoryId());
        assertEquals(List.of(2L), result.getTagIds());

        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void updateNote_whenUserIsOwner_updatesNote() {

        // Arrange: create the owner
        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        // Create an existing note
        Note note = new Note();
        note.setId(11L);
        note.setTitle("Old Title");
        note.setContent("Old Content");
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        // Pretend the owner is logged in
        loginAs("farwa-keycloak-id", "ROLE_USER");

        // Create a category
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Create a tag
        Tag tag = new Tag();
        tag.setId(2L);

        when(tagRepository.findAllById(List.of(2L)))
                .thenReturn(List.of(tag));

        // Prepare updated note details
        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setCategoryId(1L);
        request.setTagIds(List.of(2L));

        when(noteRepository.save(note))
                .thenReturn(note);

        // Act: update the note
        NoteResponseDto result =
                noteService.updateNote(11L, request);

        // Assert: check the updated values
        assertEquals(11L, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        assertEquals(1L, result.getCategoryId());
        assertEquals(List.of(2L), result.getTagIds());

        // Confirm the owner has not changed
        assertEquals(owner, note.getUser());

        verify(noteRepository).save(note);
    }

    @Test
    void updateNote_whenUserIsNotOwner_throwsAccessDeniedException() {

        // Arrange: note belongs to another user
        AppUser owner = new AppUser();
        owner.setKeycloakId("owner-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("different-user-id", "ROLE_USER");

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("Changed Title");
        request.setContent("Changed Content");
        request.setCategoryId(1L);
        request.setTagIds(List.of(2L));

        // Act and Assert
        assertThrows(
                AccessDeniedException.class,
                () -> noteService.updateNote(11L, request)
        );

        // Verify the note was not saved
        verify(noteRepository, never()).save(note);
    }

    @Test
    void deleteNote_whenUserIsAdmin_deletesNote() {

        // Arrange: note belongs to another user
        AppUser owner = new AppUser();
        owner.setKeycloakId("owner-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        // Log in as ADMIN
        loginAs("admin-keycloak-id", "ROLE_ADMIN");

        // Act
        noteService.deleteNote(11L);

        // Assert: ADMIN can delete the note
        verify(noteRepository).delete(note);
    }

    @Test
    void getAllNotes_whenAdmin_returnsAllNotes() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("user-one");

        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("First Note");
        note1.setUser(owner);

        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("Second Note");
        note2.setUser(owner);

        when(noteRepository.findAll())
                .thenReturn(List.of(note1, note2));

        loginAs("admin-keycloak-id", "ROLE_ADMIN");

        List<NoteResponseDto> result = noteService.getAllNotes();

        assertEquals(2, result.size());
        assertEquals("First Note", result.get(0).getTitle());
        assertEquals("Second Note", result.get(1).getTitle());
    }

    @Test
    void getAllNotes_whenUser_returnsOnlyOwnNotes() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        AppUser anotherUser = new AppUser();
        anotherUser.setKeycloakId("other-user-id");

        Note myNote = new Note();
        myNote.setId(1L);
        myNote.setTitle("My Note");
        myNote.setUser(owner);

        Note otherNote = new Note();
        otherNote.setId(2L);
        otherNote.setTitle("Other Note");
        otherNote.setUser(anotherUser);

        when(noteRepository.findAll())
                .thenReturn(List.of(myNote, otherNote));

        loginAs("farwa-keycloak-id", "ROLE_USER");

        List<NoteResponseDto> result = noteService.getAllNotes();

        assertEquals(1, result.size());
        assertEquals("My Note", result.get(0).getTitle());
    }

    @Test
    void getNoteById_whenNotLoggedIn_throwsAccessDeniedException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        // No JWT authentication
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> noteService.getNoteById(11L)
        );
    }

    @Test
    void createNote_whenCategoryDoesNotExist_throwsException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        loginAs("farwa-keycloak-id", "ROLE_USER");

        when(appUserRepository.findByKeycloakId("farwa-keycloak-id"))
                .thenReturn(Optional.of(owner));

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("My Note");
        request.setContent("Test content");
        request.setCategoryId(999L);
        request.setTagIds(List.of());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.createNote(request)
        );

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_whenTagDoesNotExist_throwsException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        loginAs("farwa-keycloak-id", "ROLE_USER");

        when(appUserRepository.findByKeycloakId("farwa-keycloak-id"))
                .thenReturn(Optional.of(owner));

        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // The requested tag does not exist
        when(tagRepository.findAllById(List.of(999L)))
                .thenReturn(List.of());

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("My Note");
        request.setContent("Test content");
        request.setCategoryId(1L);
        request.setTagIds(List.of(999L));

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.createNote(request)
        );

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_whenNoteDoesNotExist_throwsException() {

        // The requested note does not exist
        when(noteRepository.findById(999L))
                .thenReturn(Optional.empty());

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setCategoryId(1L);
        request.setTagIds(List.of(2L));

        // Check that the correct exception is thrown
        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.updateNote(999L, request)
        );

        // Make sure nothing is saved
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_whenCategoryDoesNotExist_throwsException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("farwa-keycloak-id", "ROLE_USER");

        // Requested category does not exist
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("Updated Note");
        request.setContent("Updated content");
        request.setCategoryId(999L);
        request.setTagIds(List.of());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.updateNote(11L, request)
        );

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_whenTagDoesNotExist_throwsException() {

        AppUser owner = new AppUser();
        owner.setKeycloakId("farwa-keycloak-id");

        Note note = new Note();
        note.setId(11L);
        note.setUser(owner);

        when(noteRepository.findById(11L))
                .thenReturn(Optional.of(note));

        loginAs("farwa-keycloak-id", "ROLE_USER");

        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Requested tag does not exist
        when(tagRepository.findAllById(List.of(999L)))
                .thenReturn(List.of());

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("Updated Note");
        request.setContent("Updated content");
        request.setCategoryId(1L);
        request.setTagIds(List.of(999L));

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.updateNote(11L, request)
        );

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_whenUserHasNoDatabaseAccount_throwsAccessDeniedException() {

        // Simulate a logged-in Keycloak user
        loginAs("unknown-keycloak-id", "ROLE_USER");

        // No matching AppUser exists in PostgreSQL
        when(appUserRepository.findByKeycloakId("unknown-keycloak-id"))
                .thenReturn(Optional.empty());

        NoteRequestDto request = new NoteRequestDto();
        request.setTitle("My Note");
        request.setContent("Test content");
        request.setCategoryId(1L);
        request.setTagIds(List.of());

        // Creating the note must be denied
        assertThrows(
                AccessDeniedException.class,
                () -> noteService.createNote(request)
        );

        // Nothing should be saved
        verify(noteRepository, never()).save(any(Note.class));
    }


}
