package com.farwa.notesapi.repository;

import com.farwa.notesapi.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Note, Long> {
}