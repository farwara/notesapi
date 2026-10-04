package com.farwa.notesapi.repository;

import com.farwa.notesapi.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}