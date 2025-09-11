package io.github.etr.playground;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.etr.playground.inventory.InventoryItem;
import io.github.etr.playground.inventory.Inventory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EntityScan(basePackageClasses = InventoryItem.class)
@EnableJpaRepositories(basePackageClasses = Inventory.class)
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
        inventory.save(new InventoryItem("DUMMY-SKU-10", 10));
        inventory.save(new InventoryItem("DUMMY-SKU-10k", 10_000));
    }

}