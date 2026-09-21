package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.TagRequestDto;
import com.farwa.notesapi.dto.TagResponseDto;
import com.farwa.notesapi.service.TagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // CREATE
    @PostMapping
    public TagResponseDto createTag(
            @Valid @RequestBody TagRequestDto requestDto) {

        return tagService.createTag(requestDto);
    }

    // GET ALL
    @GetMapping
    public List<TagResponseDto> getAllTags() {

        return tagService.getAllTags();
    }

    // GET ONE
    @GetMapping("/{id}")
    public TagResponseDto getTagById(@PathVariable Long id) {

        return tagService.getTagById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public TagResponseDto updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequestDto requestDto) {

        return tagService.updateTag(id, requestDto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id) {

        tagService.deleteTag(id);
    }
}
