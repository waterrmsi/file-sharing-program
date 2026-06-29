package com.glebkrylatov.filesharingapi.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {

    @GetMapping("/")
    public ResponseEntity<Resource> getMainPage() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML
                ).body(new ClassPathResource("static/index.html"));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> getFilePage(@PathVariable String fileId) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource("static/file.html"));
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Resource> getFavicon() {
        return ResponseEntity.ok(new ClassPathResource("imgs/favicon.png"));
    }

    @GetMapping("/logo.png")
    public ResponseEntity<Resource> getLogo() {
        return ResponseEntity.ok(new ClassPathResource("imgs/logo.png"));
    }
}
