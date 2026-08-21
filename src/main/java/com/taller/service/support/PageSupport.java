package com.taller.service.support;

import com.taller.resource.dto.PageDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageSupport {

    private PageSupport() {
    }

    public static <T> PageDTO<T> toPageDto(Page<?> page, List<T> content) {
        return new PageDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public static String normalizeTerm(String term) {
        return term == null ? "" : term.trim();
    }

    public static String normalizeSortDirection(String sortDirection) {
        return "asc".equalsIgnoreCase(sortDirection) ? "asc" : "desc";
    }

    public static PageRequest boundedPageRequest(int page, int size, int maximumSize) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), maximumSize));
    }

    public static PageRequest boundedPageRequest(int page, int size, int maximumSize, Sort sort) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), maximumSize), sort);
    }
}
