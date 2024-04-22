package restapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/public")
@Slf4j
public class PublicController
{
    @GetMapping("/")
    public void accessPublicEndpoint()
    {
        log.info("Welcome to public zone");
    }
}
