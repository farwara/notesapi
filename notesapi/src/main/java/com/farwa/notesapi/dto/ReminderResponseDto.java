package com.farwa.notesapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReminderResponseDto {

    private Long id;
    private LocalDate date;
    private LocalTime time;
    private Long noteId;
}