package com.farwa.notesapi.model;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;

    private LocalTime time;
    @OneToOne
    @JoinColumn(name = "note_id", nullable = false, unique = true)
    private Note note;
}