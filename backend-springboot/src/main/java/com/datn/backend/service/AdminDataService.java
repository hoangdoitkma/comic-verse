package com.datn.backend.service;

import com.datn.backend.dto.request.AuthorRequest;
import com.datn.backend.dto.request.GenreRequest;
import com.datn.backend.dto.request.VipPackageRequest;
import com.datn.backend.dto.response.AuthorResponse;
import com.datn.backend.dto.response.GenreResponse;
import com.datn.backend.dto.response.VipPackageResponse;

import java.util.List;

public interface AdminDataService {
    // Genre
    List<GenreResponse> getAllGenres();
    GenreResponse getGenreById(Integer id);
    GenreResponse createGenre(GenreRequest request);
    GenreResponse updateGenre(Integer id, GenreRequest request);
    void deleteGenre(Integer id);

    // Author
    List<AuthorResponse> getAllAuthors();
    AuthorResponse getAuthorById(Integer id);
    AuthorResponse createAuthor(AuthorRequest request);
    AuthorResponse updateAuthor(Integer id, AuthorRequest request);
    void deleteAuthor(Integer id);

    // VipPackage
    List<VipPackageResponse> getAllVipPackages();
    VipPackageResponse getVipPackageById(Integer id);
    VipPackageResponse createVipPackage(VipPackageRequest request);
    VipPackageResponse updateVipPackage(Integer id, VipPackageRequest request);
    void deleteVipPackage(Integer id);
}
