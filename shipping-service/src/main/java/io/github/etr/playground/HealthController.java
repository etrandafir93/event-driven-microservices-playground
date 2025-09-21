package io.github.etr.playground;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {

    @GetMapping("/isAlive")
    public String isAlive() {
        return "OK";
    }

}