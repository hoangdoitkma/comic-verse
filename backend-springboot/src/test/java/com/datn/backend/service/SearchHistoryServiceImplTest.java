package com.datn.backend.service;

import com.datn.backend.dto.public_api.request.SearchHistoryRequest;
import com.datn.backend.dto.public_api.response.HotSearchDTO;
import com.datn.backend.entity.SearchHistory;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.ContentType;
import com.datn.backend.repository.SearchHistoryRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.public_api.impl.SearchHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceImplTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchHistoryServiceImpl service;

    @Test
    void recordSearchIgnoresShortKeyword() {
        service.recordSearch(new SearchHistoryRequest("a", ContentType.COMIC), 1);

        verify(searchHistoryRepository, never()).save(any(SearchHistory.class));
    }

    @Test
    void recordSearchSkipsRecentDuplicateForSameUser() {
        User user = User.builder().id(1).email("reader@test.com").build();
        SearchHistory recent = SearchHistory.builder()
                .user(user)
                .keyword("One Piece")
                .normalizedKeyword("one piece")
                .contentType(ContentType.COMIC)
                .searchedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(searchHistoryRepository.findFirstByUserIdAndNormalizedKeywordAndContentTypeOrderBySearchedAtDesc(
                1, "one piece", ContentType.COMIC)).thenReturn(Optional.of(recent));

        service.recordSearch(new SearchHistoryRequest(" One   Piece ", ContentType.COMIC), 1);

        verify(searchHistoryRepository, never()).save(any(SearchHistory.class));
    }

    @Test
    void getHotSearchesUsesSevenDayWindow() {
        SearchHistoryRepository.HotSearchProjection projection = mock(SearchHistoryRepository.HotSearchProjection.class);
        when(projection.getKeyword()).thenReturn("one piece");
        when(projection.getContentType()).thenReturn(ContentType.COMIC);
        when(projection.getSearchCount()).thenReturn(12L);
        when(projection.getLastSearchedAt()).thenReturn(LocalDateTime.now());
        when(searchHistoryRepository.findHotSearches(any(LocalDateTime.class), eq(ContentType.COMIC), any(Pageable.class)))
                .thenReturn(List.of(projection));

        LocalDateTime beforeCall = LocalDateTime.now().minusDays(7).minusSeconds(2);
        List<HotSearchDTO> result = service.getHotSearches(ContentType.COMIC, 10);
        LocalDateTime afterCall = LocalDateTime.now().minusDays(7).plusSeconds(2);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(searchHistoryRepository).findHotSearches(sinceCaptor.capture(), eq(ContentType.COMIC), any(Pageable.class));

        assertTrue(!sinceCaptor.getValue().isBefore(beforeCall));
        assertTrue(!sinceCaptor.getValue().isAfter(afterCall));
        assertEquals(1, result.size());
        assertEquals("one piece", result.get(0).getKeyword());
        assertEquals(12L, result.get(0).getSearchCount());
    }
}
