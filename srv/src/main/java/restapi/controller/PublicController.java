package restapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/authorize")
@Slf4j
public class PublicController
{
    @GetMapping("/")
    public void accessPublicEndpoint()
    {
        log.info("Welcome to public zone");
    }
}
