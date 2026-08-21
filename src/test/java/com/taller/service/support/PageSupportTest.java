package com.taller.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PageSupportTest {

    @Test
    void convertsPageMetadataWithoutChangingValues() {
        PageImpl<String> page = new PageImpl<>(List.of("one", "two"), PageRequest.of(2, 2), 7);

        var result = PageSupport.toPageDto(page, List.of(1, 2));

        assertThat(result.content()).containsExactly(1, 2);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(7);
        assertThat(result.totalPages()).isEqualTo(4);
    }

    @Test
    void normalizesTermsAndSortDirections() {
        assertThat(PageSupport.normalizeTerm(null)).isEmpty();
        assertThat(PageSupport.normalizeTerm("  term  ")).isEqualTo("term");
        assertThat(PageSupport.normalizeSortDirection("ASC")).isEqualTo("asc");
        assertThat(PageSupport.normalizeSortDirection("anything")).isEqualTo("desc");
        assertThat(PageSupport.normalizeSortDirection(null)).isEqualTo("desc");
    }

    @Test
    void boundsPageAndSizeAndPreservesSort() {
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDateTime");

        var request = PageSupport.boundedPageRequest(-3, 500, 100, sort);

        assertThat(request.getPageNumber()).isZero();
        assertThat(request.getPageSize()).isEqualTo(100);
        assertThat(request.getSort()).isEqualTo(sort);
        assertThat(PageSupport.boundedPageRequest(1, 0, 100).getPageSize()).isEqualTo(1);
    }
}
