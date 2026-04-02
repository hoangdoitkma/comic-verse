package com.datn.backend.service.impl;

import com.datn.backend.dto.request.ComicRequest;
import com.datn.backend.dto.response.ComicResponse;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.User;
import com.datn.backend.repository.AgeRatingRepository;
import com.datn.backend.repository.AuthorRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.ComicService;
import com.datn.backend.service.S3Service;
import com.datn.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComicServiceImpl implements ComicService {

    @Autowired
    private ComicRepository comicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AgeRatingRepository ageRatingRepository;

    @Autowired
    private com.datn.backend.service.NotificationService notificationService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void notifyAdmins(Comic comic, User uploader) {
        List<User> admins = userRepository.findByRole(com.datn.backend.entity.enums.Role.ADMIN);
        String message = "Uploader " + uploader.getDisplayName() + " vừa tạo truyện mới: " + comic.getTitle() + ". Vui lòng kiểm tra.";
        for (User admin : admins) {
            notificationService.sendSystemNotification(admin, "Có truyện mới được tạo", message, com.datn.backend.entity.enums.NotificationType.SYSTEM, "/admin");
        }
    }

    private ComicResponse mapToResponse(Comic comic) {
        ComicResponse response = new ComicResponse();
        response.setId(comic.getId());
        response.setTitle(comic.getTitle());
        response.setSlug(comic.getSlug());
        response.setSynopsis(comic.getSynopsis());
        response.setThumbnailUrl(comic.getThumbnailUrl());
        response.setContentType(comic.getContentType() != null ? comic.getContentType().name() : null);
        response.setComicFormat(comic.getComicFormat() != null ? comic.getComicFormat().name() : null);
        response.setAccessType(comic.getAccessType() != null ? comic.getAccessType().name() : "FREE");
        response.setStatus(comic.getStatus() != null ? comic.getStatus().name() : null);
        response.setTotalChapters(comic.getTotalChapters());
        response.setViewCount(comic.getViewCount());
        response.setIsDeleted(comic.getIsDeleted() != null ? comic.getIsDeleted() : false);
        response.setCreatedAt(comic.getCreatedAt());
        response.setUpdatedAt(comic.getUpdatedAt());
        return response;
    }

    @Override
    @Transactional
    public ComicResponse createComic(ComicRequest request, MultipartFile thumbnail) {
        User uploader = getCurrentUser();
        // Use slug from request, fallback to sanitized title
        String folderName = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug().trim()
                : request.getTitle().replaceAll("[^a-zA-Z0-9\\-\\s]", "").replaceAll("\\s+", "-").toLowerCase();
        String thumbnailUrl = s3Service.uploadFile(thumbnail, "comics/" + folderName + "/cover");
        Comic comic = Comic.builder()
                .title(request.getTitle())
                .slug(folderName)
                .synopsis(request.getSynopsis())
                .thumbnailUrl(thumbnailUrl)
                .author(request.getAuthorId() != null
                        ? authorRepository.findById(request.getAuthorId())
                                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", request.getAuthorId()))
                        : null)
                .ageRating(request.getAgeRatingId() != null
                        ? ageRatingRepository.findById(request.getAgeRatingId())
                                .orElseThrow(() -> new ResourceNotFoundException("AgeRating", "id", request.getAgeRatingId()))
                        : null)
                .contentType(request.getContentType())
                .comicFormat(request.getComicFormat())
                .accessType(request.getAccessType() != null ? request.getAccessType() : com.datn.backend.entity.enums.AccessType.FREE)
                .status(com.datn.backend.entity.enums.ComicStatus.ONGOING)
                .createdBy(uploader)
                .build();
        Comic saved = comicRepository.save(comic);
        notifyAdmins(saved, uploader);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicResponse> getComicsByUploader() {
        User uploader = getCurrentUser();
        List<Comic> comics = comicRepository.findByCreatedByIdAndIsDeletedFalse(uploader.getId());
        return comics.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComicResponse updateComic(Integer comicId, ComicRequest request) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));
        comic.setTitle(request.getTitle());
        comic.setSynopsis(request.getSynopsis());
        comic.setContentType(request.getContentType());
        comic.setComicFormat(request.getComicFormat());
        if (request.getAccessType() != null) {
            comic.setAccessType(request.getAccessType());
        }
        // other fields can be updated as needed
        Comic updated = comicRepository.save(comic);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getComicById(Integer comicId) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));
        return mapToResponse(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ComicResponse> getAllComicsForAdmin(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("updatedAt").descending());
        org.springframework.data.domain.Page<Comic> comicsPage = comicRepository.findAll(pageable);
        return comicsPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteComic(Integer comicId) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));
        comic.setIsDeleted(true);
        comicRepository.save(comic);
    }

    @Override
    @Transactional
    public void restoreComic(Integer comicId) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));
        comic.setIsDeleted(false);
        comicRepository.save(comic);
    }
}
