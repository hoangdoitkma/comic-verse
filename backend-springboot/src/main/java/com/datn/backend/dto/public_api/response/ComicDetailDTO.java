package com.datn.backend.dto.public_api.response;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComicDetailDTO extends ComicDTO {
    private String synopsis;
    private String authorName;
    private String ageRating;
    private List<String> genres;
}
