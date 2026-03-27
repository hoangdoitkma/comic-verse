package com.datn.backend.controller;

import com.datn.backend.dto.request.AuthorRequest;
import com.datn.backend.dto.request.GenreRequest;
import com.datn.backend.dto.request.VipPackageRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.AuthorResponse;
import com.datn.backend.dto.response.GenreResponse;
import com.datn.backend.dto.response.VipPackageResponse;
import com.datn.backend.service.AdminDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDataController {

    private final AdminDataService adminDataService;

    // --- GENRES ---
    @GetMapping("/genres")
    public ApiResponse<List<GenreResponse>> getAllGenres() {
        return ApiResponse.success(adminDataService.getAllGenres());
    }

    @GetMapping("/genres/{id}")
    public ApiResponse<GenreResponse> getGenreById(@PathVariable Integer id) {
        return ApiResponse.success(adminDataService.getGenreById(id));
    }

    @PostMapping("/genres")
    public ApiResponse<GenreResponse> createGenre(@Valid @RequestBody GenreRequest request) {
        return ApiResponse.success(adminDataService.createGenre(request), "Genre created successfully");
    }

    @PutMapping("/genres/{id}")
    public ApiResponse<GenreResponse> updateGenre(@PathVariable Integer id, @Valid @RequestBody GenreRequest request) {
        return ApiResponse.success(adminDataService.updateGenre(id, request), "Genre updated successfully");
    }

    @DeleteMapping("/genres/{id}")
    public ApiResponse<Void> deleteGenre(@PathVariable Integer id) {
        adminDataService.deleteGenre(id);
        return ApiResponse.success(null, "Genre deleted successfully");
    }

    // --- AUTHORS ---
    @GetMapping("/authors")
    public ApiResponse<List<AuthorResponse>> getAllAuthors() {
        return ApiResponse.success(adminDataService.getAllAuthors());
    }

    @GetMapping("/authors/{id}")
    public ApiResponse<AuthorResponse> getAuthorById(@PathVariable Integer id) {
        return ApiResponse.success(adminDataService.getAuthorById(id));
    }

    @PostMapping("/authors")
    public ApiResponse<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest request) {
        return ApiResponse.success(adminDataService.createAuthor(request), "Author created successfully");
    }

    @PutMapping("/authors/{id}")
    public ApiResponse<AuthorResponse> updateAuthor(@PathVariable Integer id, @Valid @RequestBody AuthorRequest request) {
        return ApiResponse.success(adminDataService.updateAuthor(id, request), "Author updated successfully");
    }

    @DeleteMapping("/authors/{id}")
    public ApiResponse<Void> deleteAuthor(@PathVariable Integer id) {
        adminDataService.deleteAuthor(id);
        return ApiResponse.success(null, "Author deleted successfully");
    }

    // --- VIP PACKAGES ---
    @GetMapping("/vip-packages")
    public ApiResponse<List<VipPackageResponse>> getAllVipPackages() {
        return ApiResponse.success(adminDataService.getAllVipPackages());
    }

    @GetMapping("/vip-packages/{id}")
    public ApiResponse<VipPackageResponse> getVipPackageById(@PathVariable Integer id) {
        return ApiResponse.success(adminDataService.getVipPackageById(id));
    }

    @PostMapping("/vip-packages")
    public ApiResponse<VipPackageResponse> createVipPackage(@Valid @RequestBody VipPackageRequest request) {
        return ApiResponse.success(adminDataService.createVipPackage(request), "VIP Package created successfully");
    }

    @PutMapping("/vip-packages/{id}")
    public ApiResponse<VipPackageResponse> updateVipPackage(@PathVariable Integer id, @Valid @RequestBody VipPackageRequest request) {
        return ApiResponse.success(adminDataService.updateVipPackage(id, request), "VIP Package updated successfully");
    }

    @DeleteMapping("/vip-packages/{id}")
    public ApiResponse<Void> deleteVipPackage(@PathVariable Integer id) {
        adminDataService.deleteVipPackage(id);
        return ApiResponse.success(null, "VIP Package deleted successfully");
    }
}
