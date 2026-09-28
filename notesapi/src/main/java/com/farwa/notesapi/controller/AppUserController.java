package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.AppUserRequestDto;
import com.farwa.notesapi.dto.AppUserResponseDto;
import com.farwa.notesapi.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    public AppUserResponseDto createUser(
            @Valid @RequestBody AppUserRequestDto requestDto) {

        return appUserService.createUser(requestDto);
    }
}