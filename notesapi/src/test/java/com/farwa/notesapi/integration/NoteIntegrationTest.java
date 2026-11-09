
package com.farwa.notesapi.integration;

import com.farwa.notesapi.model.AppUser;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.repository.AppUserRepository;
import com.farwa.notesapi.repository.CategoryRepository;
import com.farwa.notesapi.repository.NoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NoteRepository noteRepository;

    // Prevent external Keycloak calls during tests.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Long categoryId;

    @BeforeEach
    void setUp() {

        // Remove records from previous tests.
        noteRepository.deleteAll();
        categoryRepository.deleteAll();
        appUserRepository.deleteAll();

        // Create a user linked to the simulated JWT.
        AppUser user = new AppUser();
        user.setKeycloakId("integration-user-id");
        user.setUsername("integrationuser");
        user.setEmail("integration@example.com");
        appUserRepository.save(user);

        // Create a category.
        Category category = new Category();
        category.setName("Integration Category");

        categoryId = categoryRepository.save(category).getId();
    }

    // TEST 1: Create a note through the real application.
    @Test
    void createNote_savesNoteInDatabase() throws Exception {

        String requestBody = """
                {
                  "title": "Integration Note",
                  "content": "Saved in the test database",
                  "categoryId": %d,
                  "tagIds": []
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/notes")
                                .with(jwt()
                                        .jwt(token -> token.subject(
                                                "integration-user-id"
                                        ))
                                        .authorities(() -> "ROLE_USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Integration Note"))
                .andExpect(jsonPath("$.content")
                        .value("Saved in the test database"))
                .andExpect(jsonPath("$.categoryId")
                        .value(categoryId.intValue()));

        // Verify that the real repository saved the note.
        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                noteRepository.count()
        );
    }

    // TEST 2: Retrieve a note from the real database.
    @Test
    void getNoteById_returnsSavedNote() throws Exception {

        AppUser user = appUserRepository
                .findByKeycloakId("integration-user-id")
                .orElseThrow();

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow();

        com.farwa.notesapi.model.Note note =
                new com.farwa.notesapi.model.Note();

        note.setTitle("Existing Note");
        note.setContent("Stored in H2");
        note.setUser(user);
        note.setCategory(category);
        note.setTags(java.util.List.of());

        Long noteId = noteRepository.save(note).getId();

        mockMvc.perform(
                        get("/api/notes/{id}", noteId)
                                .with(jwt()
                                        .jwt(token -> token.subject(
                                                "integration-user-id"
                                        ))
                                        .authorities(() -> "ROLE_USER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(noteId.intValue()))
                .andExpect(jsonPath("$.title")
                        .value("Existing Note"))
                .andExpect(jsonPath("$.content")
                        .value("Stored in H2"));
    }
}
