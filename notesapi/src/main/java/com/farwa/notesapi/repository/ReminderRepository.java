package com.farwa.notesapi.repository;

import com.farwa.notesapi.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Note, Long> {
}
