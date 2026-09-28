package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.AppUserRequestDto;
import com.farwa.notesapi.dto.AppUserResponseDto;
import com.farwa.notesapi.model.AppUser;
import com.farwa.notesapi.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUserResponseDto createUser(AppUserRequestDto requestDto) {

        AppUser user = new AppUser();

        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());

        AppUser savedUser = appUserRepository.save(user);

        return mapToResponse(savedUser);
    }

    private AppUserResponseDto mapToResponse(AppUser user) {

        AppUserResponseDto responseDto = new AppUserResponseDto();

        responseDto.setId(user.getId());
        responseDto.setUsername(user.getUsername());
        responseDto.setEmail(user.getEmail());

        return responseDto;
    }
}