package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.TagRequestDto;
import com.farwa.notesapi.dto.TagResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Tag;
import com.farwa.notesapi.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // CREATE
    public TagResponseDto createTag(TagRequestDto requestDto) {

        Tag tag = new Tag();
        tag.setName(requestDto.getName());

        Tag savedTag = tagRepository.save(tag);

        return mapToResponse(savedTag);
    }

    // GET ONE
    public TagResponseDto getTagById(Long id) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tag not found"));

        return mapToResponse(tag);
    }

    // GET ALL
    public List<TagResponseDto> getAllTags() {

        return tagRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // UPDATE
    public TagResponseDto updateTag(Long id, TagRequestDto requestDto) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tag not found"));

        tag.setName(requestDto.getName());

        Tag updatedTag = tagRepository.save(tag);

        return mapToResponse(updatedTag);
    }

    // DELETE
    public void deleteTag(Long id) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tag not found"));

        tagRepository.delete(tag);
    }

    // ENTITY → RESPONSE DTO
    private TagResponseDto mapToResponse(Tag tag) {

        TagResponseDto responseDto = new TagResponseDto();

        responseDto.setId(tag.getId());
        responseDto.setName(tag.getName());

        return responseDto;
    }
}