package io.github.etr.playground;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import io.github.etr.playground.inventory.Inventory;
import io.github.etr.playground.inventory.InventoryItem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
class InventoryServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApp.class, args);
    }


    @Autowired
    private Inventory inventory;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("adding dummy inventory items");
        inventory.saveAll(List.of(
            new InventoryItem("DUMMY-SKU-10", 10),
            new InventoryItem("DUMMY-SKU-10k", 10_000),
            new InventoryItem("TV-55-SAM-QLED", 100),
            new InventoryItem("PHN-APL-IP15-BLK-128", 100),
            new InventoryItem("LTP-DEL-XPS13-512", 100)));
    }

}