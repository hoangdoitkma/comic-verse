package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.response.ComicDTO;
import com.datn.backend.dto.public_api.response.ComicDetailDTO;
import com.datn.backend.dto.public_api.response.HomeDataResponse;
import com.datn.backend.dto.public_api.response.ReadingHistoryInfoDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PublicComicService {
    Page<ComicDTO> getComics(int page, int limit);
    ComicDetailDTO getComicDetail(String slug);
    HomeDataResponse getHomeContent();
    List<ReadingHistoryInfoDTO> getReadingHistoryInfo(List<Integer> comicIds, Integer userId);
}
