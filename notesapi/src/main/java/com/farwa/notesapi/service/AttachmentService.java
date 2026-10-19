
package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.AttachmentResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Attachment;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.repository.AttachmentRepository;
import com.farwa.notesapi.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_FILE_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");

    private final Path uploadDirectory = Paths.get("uploads");

    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            NoteRepository noteRepository) {

        this.attachmentRepository = attachmentRepository;
        this.noteRepository = noteRepository;
    }

    // UPLOAD ATTACHMENT
    public AttachmentResponseDto uploadAttachment(
            Long noteId,
            MultipartFile file) {

        // 1. Check if the file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a non-empty file");
        }

        // 2. Check the file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB");
        }

        // 3. Check the file type
        String fileType = file.getContentType();

        if (fileType == null ||
                !ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new IllegalArgumentException(
                    "Only PDF, JPG and PNG files are allowed");
        }

        // 4. Find the Note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Note not found"));

        // 5. Check if the Note already has an attachment
        if (note.getAttachment() != null) {
            throw new IllegalArgumentException(
                    "This note already has an attachment");
        }

        // 6. Check the original filename
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File must have a name");
        }

        // 7. Keep a simple, safe display name
        String safeFileName = originalFileName
                .replace("\\", "/");

        safeFileName = safeFileName.substring(
                safeFileName.lastIndexOf("/") + 1);

        if (safeFileName.isBlank() ||
                safeFileName.equals(".") ||
                safeFileName.equals("..")) {
            throw new IllegalArgumentException(
                    "Invalid filename");
        }

        // 8. Generate a unique stored filename
        String storedFileName = UUID.randomUUID().toString();

        Path filePath = uploadDirectory.resolve(storedFileName);

        // 9. Save the physical file
        try {
            Files.createDirectories(uploadDirectory);

            Files.copy(file.getInputStream(), filePath);

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save uploaded file",
                    exception);
        }

        // 10. Create the Attachment entity
        Attachment attachment = new Attachment();

        attachment.setFileName(safeFileName);
        attachment.setFileType(fileType);
        attachment.setFilePath(filePath.toString());
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setNote(note);

        // 11. Save the metadata in PostgreSQL
        Attachment savedAttachment;

        try {
            savedAttachment = attachmentRepository.saveAndFlush(
                    attachment);
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }

        // 12. Return the response DTO
        return mapToResponseDto(savedAttachment);
    }

    // GET ATTACHMENT INFORMATION
    public AttachmentResponseDto getAttachmentById(Long id) {

        Attachment attachment = getAttachmentEntity(id);

        return mapToResponseDto(attachment);
    }

    // DOWNLOAD ATTACHMENT
    public byte[] downloadAttachment(Long id) {

        Attachment attachment = getAttachmentEntity(id);

        Path filePath = Paths.get(attachment.getFilePath());

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not download attachment",
                    exception);
        }
    }

    // GET ATTACHMENT ENTITY
    public Attachment getAttachmentEntity(Long id) {

        return attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found"));
    }

    // DELETE ATTACHMENT
    public void deleteAttachment(Long id) {

        Attachment attachment = getAttachmentEntity(id);

        Path filePath = Paths.get(attachment.getFilePath());

        Note note = attachment.getNote();

        if (note != null) {
            note.setAttachment(null);
        }

        attachmentRepository.delete(attachment);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Attachment metadata was deleted, but the file could not be removed",
                    exception);
        }
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private AttachmentResponseDto mapToResponseDto(
            Attachment attachment) {

        AttachmentResponseDto responseDto =
                new AttachmentResponseDto();

        responseDto.setId(attachment.getId());
        responseDto.setFileName(attachment.getFileName());
        responseDto.setFileType(attachment.getFileType());
        responseDto.setUploadDate(attachment.getUploadDate());
        responseDto.setNoteId(attachment.getNote().getId());

        return responseDto;
    }
}
