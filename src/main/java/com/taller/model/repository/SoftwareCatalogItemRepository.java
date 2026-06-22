package com.taller.model.repository;

import com.taller.model.SoftwareCatalogItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareCatalogItemRepository extends JpaRepository<SoftwareCatalogItem, String> {

    List<SoftwareCatalogItem> findAllByOrderByNameAsc();
}
