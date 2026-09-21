package  com.farwa.notesapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagRequestDto {

    @NotBlank
    private String name;
}