package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.AttachmentResponseDto;
import com.farwa.notesapi.model.Attachment;
import com.farwa.notesapi.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }


    // UPLOAD FILE
    @PostMapping("/{noteId}")
    public AttachmentResponseDto uploadAttachment(
            @PathVariable Long noteId,
            @RequestParam("file") MultipartFile file) {

        return attachmentService.uploadAttachment(noteId, file);
    }


    // GET ATTACHMENT INFORMATION
    @GetMapping("/{id}")
    public AttachmentResponseDto getAttachmentById(
            @PathVariable Long id) {

        return attachmentService.getAttachmentById(id);
    }


    // DOWNLOAD ACTUAL FILE
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long id) {

        Attachment attachment =
                attachmentService.getAttachmentEntity(id);

        byte[] file =
                attachmentService.downloadAttachment(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getFileType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                attachment.getFileName() + "\""
                )
                .body(file);
    }


    // DELETE ATTACHMENT
    @DeleteMapping("/{id}")
    public void deleteAttachment(
            @PathVariable Long id) {

        attachmentService.deleteAttachment(id);
    }
}