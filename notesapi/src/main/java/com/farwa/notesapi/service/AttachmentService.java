
package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.AttachmentResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Attachment;
import com.farwa.notesapi.model.Note;
import com.farwa.notesapi.repository.AttachmentRepository;
import com.farwa.notesapi.repository.NoteRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
            Set.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            );

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            NoteRepository noteRepository) {

        this.attachmentRepository = attachmentRepository;
        this.noteRepository = noteRepository;
    }

    // 1. UPLOAD ATTACHMENT
    public AttachmentResponseDto uploadAttachment(
            Long noteId,
            MultipartFile file) {

        // Find the note and check ownership FIRST
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found"));

        checkNoteOwnership(note);

        // Check if the file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a non-empty file");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB");
        }

        // Check file type
        String fileType = file.getContentType();

        if (fileType == null ||
                !ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new IllegalArgumentException(
                    "Only PDF, JPG and PNG files are allowed");
        }

        // Check if the note already has an attachment
        if (note.getAttachment() != null) {
            throw new IllegalArgumentException(
                    "This note already has an attachment");
        }

        // Check original filename
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File must have a name");
        }

        // Create a safe display filename
        String safeFileName =
                originalFileName.replace("\\", "/");

        safeFileName = safeFileName.substring(
                safeFileName.lastIndexOf("/") + 1);

        if (safeFileName.isBlank() ||
                safeFileName.equals(".") ||
                safeFileName.equals("..")) {
            throw new IllegalArgumentException(
                    "Invalid filename");
        }

        // Generate a unique stored filename
        String storedFileName = UUID.randomUUID().toString();

        Path filePath = uploadDirectory
                .resolve(storedFileName)
                .normalize();

        // Make sure the file stays inside uploads
        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid file path");
        }

        // Save physical file
        try {
            Files.createDirectories(uploadDirectory);

            Files.copy(file.getInputStream(), filePath);

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save uploaded file",
                    exception);
        }

        // Create attachment entity
        Attachment attachment = new Attachment();

        attachment.setFileName(safeFileName);
        attachment.setFileType(fileType);
        attachment.setFilePath(filePath.toString());
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setNote(note);

        // Save metadata in PostgreSQL
        Attachment savedAttachment;

        try {
            savedAttachment =
                    attachmentRepository.saveAndFlush(attachment);

        } catch (RuntimeException exception) {

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }

        return mapToResponseDto(savedAttachment);
    }

    // 2. GET ATTACHMENT INFORMATION
    public AttachmentResponseDto getAttachmentById(Long id) {

        Attachment attachment = getAttachmentEntity(id);

        return mapToResponseDto(attachment);
    }

    // 3. DOWNLOAD ATTACHMENT
    public byte[] downloadAttachment(Long id) {

        Attachment attachment = getAttachmentEntity(id);

        Path filePath = getSafeFilePath(
                attachment.getFilePath());

        try {
            return Files.readAllBytes(filePath);

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not download attachment",
                    exception);
        }
    }

    // 4. GET ATTACHMENT ENTITY
    public Attachment getAttachmentEntity(Long id) {

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found"));

        checkNoteOwnership(attachment.getNote());

        return attachment;
    }

    // 5. DELETE ATTACHMENT
    public void deleteAttachment(Long id) {

        Attachment attachment = getAttachmentEntity(id);

        Path filePath = getSafeFilePath(
                attachment.getFilePath());

        Note note = attachment.getNote();

        if (note != null) {
            note.setAttachment(null);
        }

        attachmentRepository.delete(attachment);
        attachmentRepository.flush();

        try {
            Files.deleteIfExists(filePath);

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Attachment metadata was deleted, "
                            + "but the file could not be removed",
                    exception);
        }
    }

    // 6. CHECK NOTE OWNERSHIP USING KEYCLOAK
    private void checkNoteOwnership(Note note) {

        if (!(SecurityContextHolder.getContext()
                .getAuthentication()
                instanceof JwtAuthenticationToken authentication)) {

            throw new AccessDeniedException(
                    "You must be logged in");
        }

        boolean isAdmin = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));

        // ADMIN can access all attachments
        if (isAdmin) {
            return;
        }

        String keycloakId =
                authentication.getToken().getSubject();

        if (note == null ||
                note.getUser() == null ||
                !keycloakId.equals(
                        note.getUser().getKeycloakId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this attachment");
        }
    }

    // 7. CHECK THAT STORED FILE PATH IS SAFE
    private Path getSafeFilePath(String storedPath) {

        if (storedPath == null || storedPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid attachment file path");
        }

        Path filePath = Paths.get(storedPath)
                .toAbsolutePath()
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Attachment path is outside upload directory");
        }

        return filePath;
    }

    // 8. CONVERT ENTITY TO RESPONSE DTO
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

