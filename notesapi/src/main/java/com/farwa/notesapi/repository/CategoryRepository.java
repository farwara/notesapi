package com.farwa.notesapi.repository;

import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
