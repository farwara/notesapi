package com.farwa.notesapi.repository;

import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}