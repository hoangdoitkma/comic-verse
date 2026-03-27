package com.datn.backend.service.impl;

import com.datn.backend.dto.request.AuthorRequest;
import com.datn.backend.dto.request.GenreRequest;
import com.datn.backend.dto.request.VipPackageRequest;
import com.datn.backend.dto.response.AuthorResponse;
import com.datn.backend.dto.response.GenreResponse;
import com.datn.backend.dto.response.VipPackageResponse;
import com.datn.backend.entity.Author;
import com.datn.backend.entity.Genre;
import com.datn.backend.entity.VipPackage;
import com.datn.backend.exception.ResourceInUseException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.AuthorRepository;
import com.datn.backend.repository.ComicGenreRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.GenreRepository;
import com.datn.backend.repository.VipPackageRepository;
import com.datn.backend.service.AdminDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDataServiceImpl implements AdminDataService {

    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;
    private final VipPackageRepository vipPackageRepository;
    private final ComicRepository comicRepository;
    private final ComicGenreRepository comicGenreRepository;

    // --- GENRE ---
    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::mapToGenreResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GenreResponse getGenreById(Integer id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        return mapToGenreResponse(genre);
    }

    @Override
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Genre name already exists");
        }
        Genre genre = Genre.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToGenreResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(Integer id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        if (!genre.getName().equals(request.getName()) && genreRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Genre name already exists");
        }
        genre.setName(request.getName());
        genre.setDescription(request.getDescription());
        return mapToGenreResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public void deleteGenre(Integer id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Genre not found with id: " + id);
        }
        if (comicGenreRepository.existsByGenreId(id)) {
            throw new ResourceInUseException("Cannot delete genre because it is being used by one or more comics");
        }
        genreRepository.deleteById(id);
    }

    // --- AUTHOR ---
    @Override
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::mapToAuthorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorResponse getAuthorById(Integer id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        return mapToAuthorResponse(author);
    }

    @Override
    @Transactional
    public AuthorResponse createAuthor(AuthorRequest request) {
        Author author = Author.builder()
                .name(request.getName())
                .studio(request.getStudio())
                .country(request.getCountry())
                .build();
        return mapToAuthorResponse(authorRepository.save(author));
    }

    @Override
    @Transactional
    public AuthorResponse updateAuthor(Integer id, AuthorRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        author.setName(request.getName());
        author.setStudio(request.getStudio());
        author.setCountry(request.getCountry());
        return mapToAuthorResponse(authorRepository.save(author));
    }

    @Override
    @Transactional
    public void deleteAuthor(Integer id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        if (comicRepository.existsByAuthorId(id)) {
            throw new ResourceInUseException("Cannot delete author because they are associated with one or more comics");
        }
        authorRepository.deleteById(id);
    }

    // --- VIP PACKAGE ---
    @Override
    public List<VipPackageResponse> getAllVipPackages() {
        return vipPackageRepository.findAll().stream()
                .map(this::mapToVipPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VipPackageResponse getVipPackageById(Integer id) {
        VipPackage vipPackage = vipPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VipPackage not found with id: " + id));
        return mapToVipPackageResponse(vipPackage);
    }

    @Override
    @Transactional
    public VipPackageResponse createVipPackage(VipPackageRequest request) {
        if (vipPackageRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("VipPackage name already exists");
        }
        VipPackage vipPackage = VipPackage.builder()
                .name(request.getName())
                .durationMonth(request.getDurationMonth())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return mapToVipPackageResponse(vipPackageRepository.save(vipPackage));
    }

    @Override
    @Transactional
    public VipPackageResponse updateVipPackage(Integer id, VipPackageRequest request) {
        VipPackage vipPackage = vipPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VipPackage not found with id: " + id));
        
        if (!vipPackage.getName().equals(request.getName()) && vipPackageRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("VipPackage name already exists");
        }
        
        vipPackage.setName(request.getName());
        vipPackage.setDurationMonth(request.getDurationMonth());
        vipPackage.setPrice(request.getPrice());
        vipPackage.setCurrency(request.getCurrency());
        if (request.getIsActive() != null) {
            vipPackage.setIsActive(request.getIsActive());
        }
        
        return mapToVipPackageResponse(vipPackageRepository.save(vipPackage));
    }

    @Override
    @Transactional
    public void deleteVipPackage(Integer id) {
        if (!vipPackageRepository.existsById(id)) {
            throw new ResourceNotFoundException("VipPackage not found with id: " + id);
        }
        vipPackageRepository.deleteById(id);
    }

    // --- MAPPERS ---
    private GenreResponse mapToGenreResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();
    }

    private AuthorResponse mapToAuthorResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .studio(author.getStudio())
                .country(author.getCountry())
                .createdAt(author.getCreatedAt())
                .build();
    }

    private VipPackageResponse mapToVipPackageResponse(VipPackage vipPackage) {
        return VipPackageResponse.builder()
                .id(vipPackage.getId())
                .name(vipPackage.getName())
                .durationMonth(vipPackage.getDurationMonth())
                .price(vipPackage.getPrice())
                .currency(vipPackage.getCurrency())
                .isActive(vipPackage.getIsActive())
                .createdAt(vipPackage.getCreatedAt())
                .build();
    }
}
