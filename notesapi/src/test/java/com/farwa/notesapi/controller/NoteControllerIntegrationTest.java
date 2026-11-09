
package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.NoteRequestDto;
import com.farwa.notesapi.dto.NoteResponseDto;
import com.farwa.notesapi.service.NoteService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    // TEST 1: Get all notes
    @Test
    void getAllNotes_returnsOk() throws Exception {

        when(noteService.getAllNotes())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/notes")
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // TEST 2: Get note by ID
    @Test
    void getNoteById_returnsOk() throws Exception {

        NoteResponseDto response = new NoteResponseDto();
        response.setId(1L);
        response.setTitle("My Note");
        response.setContent("Learning Spring Boot");

        when(noteService.getNoteById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/notes/1")
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("My Note"))
                .andExpect(jsonPath("$.content").value("Learning Spring Boot"));
    }

    // TEST 3: Create a note
    @Test
    void createNote_returnsOk() throws Exception {

        NoteResponseDto response = new NoteResponseDto();
        response.setId(2L);
        response.setTitle("New Note");
        response.setContent("My new content");

        when(noteService.createNote(any(NoteRequestDto.class)))
                .thenReturn(response);

        String requestBody = """
                {
                  "title": "New Note",
                  "content": "My new content",
                  "categoryId": 1,
                  "tagIds": [2]
                }
                """;

        mockMvc.perform(
                        post("/api/notes")
                                .with(jwt())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("New Note"));
    }

    // TEST 4: Update a note
    @Test
    void updateNote_returnsOk() throws Exception {

        NoteResponseDto response = new NoteResponseDto();
        response.setId(1L);
        response.setTitle("Updated Note");
        response.setContent("Updated content");

        when(noteService.updateNote(
                eq(1L),
                any(NoteRequestDto.class)
        )).thenReturn(response);

        String requestBody = """
                {
                  "title": "Updated Note",
                  "content": "Updated content",
                  "categoryId": 1,
                  "tagIds": [2]
                }
                """;

        mockMvc.perform(
                        put("/api/notes/1")
                                .with(jwt())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Note"))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    // TEST 5: Delete a note
    @Test
    void deleteNote_returnsOk() throws Exception {

        mockMvc.perform(
                        delete("/api/notes/1")
                                .with(jwt())
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }

    // TEST 6: Unauthenticated request is rejected
    @Test
    void getAllNotes_withoutJwt_returnsUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }
}
