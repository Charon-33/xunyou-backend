package com.wsj.xunyou.controller;

import com.wsj.xunyou.utils.FileUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class FileController {
    @PostMapping("/upload")
    public String uploadImage(@RequestParam MultipartFile file) {
        return FileUpload.uploadImage(file, "d:/dev/xunyouImg/");
    }

}
