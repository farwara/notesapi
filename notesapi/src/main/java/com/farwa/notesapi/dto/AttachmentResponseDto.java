package com.farwa.notesapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttachmentResponseDto {

    private Long id;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadDate;
    private Long noteId;
}