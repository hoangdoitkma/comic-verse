package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.response.ComicDTO;
import com.datn.backend.dto.public_api.response.ComicDetailDTO;
import com.datn.backend.dto.public_api.response.HomeDataResponse;
import com.datn.backend.dto.public_api.response.ReadingHistoryInfoDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PublicComicService {
    Page<ComicDTO> getComics(int page, int limit);
    Page<ComicDTO> searchComics(int page, int limit,
                                String keyword,
                                com.datn.backend.entity.enums.ContentType type,
                                com.datn.backend.entity.enums.OriginCountry country,
                                Integer genreId,
                                com.datn.backend.entity.enums.ComicStatus status);
    ComicDetailDTO getComicDetail(String slug);
    HomeDataResponse getHomeContent(com.datn.backend.entity.enums.ContentType type, Integer userId);
    List<ReadingHistoryInfoDTO> getReadingHistoryInfo(List<Integer> comicIds, Integer userId);
}
