package com.farwa.notesapi.repository;

import com.farwa.notesapi.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}