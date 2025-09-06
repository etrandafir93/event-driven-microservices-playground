package io.github.etr.playground.inventory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class InventoryController {

	@GetMapping("/hello")
	String hello() {
		return "Hello from Inventory Service!";
	}

}