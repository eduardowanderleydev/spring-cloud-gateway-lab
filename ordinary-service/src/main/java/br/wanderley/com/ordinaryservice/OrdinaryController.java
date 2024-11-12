package br.wanderley.com.ordinaryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greetings")
public class OrdinaryController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello!";
    }

}
