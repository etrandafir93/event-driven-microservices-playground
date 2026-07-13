package io.github.etr.demo.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoClientController {

    private final DemoClient demoClient;
    private final ToxiproxyClient toxiproxy;

    @GetMapping
    public String page(Model model) {
        model.addAttribute("getRate", demoClient.getGetRate());
        model.addAttribute("postRate", demoClient.getPostRate());
        model.addAttribute("dbLatency", toxiproxy.getLatency(ToxiproxyClient.DB_PROXY, ToxiproxyClient.DB_TOXIC));
        model.addAttribute("loyaltyLatency", toxiproxy.getLatency(ToxiproxyClient.LOYALTY_PROXY, ToxiproxyClient.LOYALTY_TOXIC));
        return "demo";
    }

    @PostMapping
    public String update(@RequestParam int getRate,
                         @RequestParam int postRate,
                         @RequestParam int dbLatency,
                         @RequestParam int loyaltyLatency) {
        demoClient.setGetRate(getRate);
        demoClient.setPostRate(postRate);
        toxiproxy.setLatency(ToxiproxyClient.DB_PROXY, ToxiproxyClient.DB_TOXIC, dbLatency);
        toxiproxy.setLatency(ToxiproxyClient.LOYALTY_PROXY, ToxiproxyClient.LOYALTY_TOXIC, loyaltyLatency);
        return "redirect:/demo";
    }
}
