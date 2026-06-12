package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.response.ComicDTO;
import com.datn.backend.entity.enums.ContentType;

import java.util.List;

public interface RecommendationService {
    /**
     * Lấy danh sách truyện đề xuất dựa trên lịch sử đọc của user.
     * Phân tích genre từ các truyện đã đọc → tìm truyện chưa đọc cùng genre.
     *
     * @param userId    ID người dùng (null nếu chưa login → fallback trending)
     * @param type      Loại nội dung (COMIC/NOVEL), null = tất cả
     * @return Danh sách truyện đề xuất (tối đa 10)
     */
    List<ComicDTO> getRecommendedComics(Integer userId, ContentType type);

    /**
     * Lấy danh sách truyện đề xuất cho màn hình chi tiết truyện hiện tại.
     * Dựa theo tác giả, thể loại của truyện hiện tại và lịch sử đọc của người dùng.
     */
    List<ComicDTO> getSimilarComics(String slug, Integer userId);
}
