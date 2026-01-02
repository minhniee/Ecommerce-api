package com.example.auth_shop.controller;

import com.example.auth_shop.dto.ImageDto;
import com.example.auth_shop.model.Image;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/images")
public class ImageController {

    private final IImageService imageService;

    @PostMapping
    public ResponseEntity<APIResponse> uploadImages(
            @RequestParam List<MultipartFile> files,
            @RequestParam Long productId) {
        List<ImageDto> imageDtos = imageService.saveImage(files, productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.created("Images uploaded successfully", imageDtos));
    }

    @GetMapping("/{imageId}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId) throws SQLException {
        Image image = imageService.getImageById(imageId);
        ByteArrayResource resource = new ByteArrayResource(
                image.getImage().getBytes(1, (int) image.getImage().length()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(resource);
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<APIResponse> updateImage(
            @PathVariable Long imageId, 
            @RequestBody MultipartFile file) {
        imageService.updateImage(file, imageId);
        return ResponseEntity.ok(APIResponse.success("Image updated successfully"));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<APIResponse> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImageById(imageId);
        return ResponseEntity.ok(APIResponse.success("Image deleted successfully"));
    }
}
