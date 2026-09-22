package com.farwa.notesapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppUserResponseDto {

    private Long id;
    private String username;
    private String email;
}
