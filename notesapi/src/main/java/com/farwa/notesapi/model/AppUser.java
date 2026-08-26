package com.farwa.notesapi.model;
import jakarta.persistence.*;
import jakarta.persistence.OneToMany;
import java.util.List;
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user")
    private List<Note> notes;
}

