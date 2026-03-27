package com.datn.backend.service;

import com.datn.backend.dto.request.ComicRequest;
import com.datn.backend.dto.response.ComicResponse;
import java.util.List;

public interface ComicService {
    ComicResponse createComic(ComicRequest request, org.springframework.web.multipart.MultipartFile thumbnail);
    List<ComicResponse> getComicsByUploader();
    ComicResponse updateComic(Integer comicId, ComicRequest request);
    ComicResponse getComicById(Integer comicId);
}
