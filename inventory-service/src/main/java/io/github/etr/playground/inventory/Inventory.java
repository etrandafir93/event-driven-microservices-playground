package io.github.etr.playground.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Inventory extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByItemSku(String itemSku);

}