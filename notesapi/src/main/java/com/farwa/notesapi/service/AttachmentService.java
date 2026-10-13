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
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
public class AttachmentService {

    private final Path uploadDirectory = Paths.get("uploads");

    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            NoteRepository noteRepository) {

        this.attachmentRepository = attachmentRepository;
        this.noteRepository = noteRepository;
    }

    // UPLOAD
    public AttachmentResponseDto uploadAttachment(
            Long noteId,
            MultipartFile file) {

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note not found"));

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not create upload directory",
                    exception
            );
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("File must have a name");
        }

        String safeFileName = Paths.get(originalFileName)
                .getFileName()
                .toString();

        Path filePath = uploadDirectory.resolve(safeFileName);

        try {
            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save uploaded file",
                    exception
            );
        }

        Attachment attachment = new Attachment();

        attachment.setFileName(safeFileName);
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(filePath.toString());
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setNote(note);

        Attachment savedAttachment =
                attachmentRepository.save(attachment);

        return mapToResponseDto(savedAttachment);
    }


    // GET ATTACHMENT INFORMATION
    public AttachmentResponseDto getAttachmentById(Long id) {

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found"));

        return mapToResponseDto(attachment);
    }


    // GET THE ACTUAL FILE FOR DOWNLOAD
    public byte[] downloadAttachment(Long id) {

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found"));

        Path filePath = Paths.get(attachment.getFilePath());

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not read attachment file",
                    exception
            );
        }
    }


    // Used by the controller when downloading
    public Attachment getAttachmentEntity(Long id) {

        return attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found"));
    }


    // DELETE
    public void deleteAttachment(Long id) {

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found"));

        Path filePath = Paths.get(attachment.getFilePath());

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not delete attachment file",
                    exception
            );
        }

        Note note = attachment.getNote();

        if (note != null) {
            note.setAttachment(null);
        }

        attachmentRepository.delete(attachment);
    }


    // ENTITY -> DTO
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