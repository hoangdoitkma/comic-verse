package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataResponse {
    private List<ComicDTO> topTrending;
    private List<ComicDTO> recentlyUpdated;
    private List<ComicDTO> newComics;
    private List<ComicDTO> recommended;
}
