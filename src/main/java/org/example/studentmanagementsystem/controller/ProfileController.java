package org.example.studentmanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.studentmanagementsystem.config.OpenApiConfig;
import org.example.studentmanagementsystem.dto.ProfileResponse;
import org.example.studentmanagementsystem.service.AuthService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Profile", description = "The logged-in student's own profile and photo")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;

    @Operation(summary = "Get my profile", description = "Identified from the JWT, so no id is needed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public ProfileResponse me(Authentication authentication) {
        return authService.getProfile(authentication.getName());
    }

    @Operation(summary = "Upload or replace my photo", description = "multipart/form-data, part name 'file', JPEG or PNG, max 2MB.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uploaded"),
            @ApiResponse(responseCode = "400", description = "Not a JPEG/PNG image")
    })
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadPhoto(Authentication authentication,
                              @RequestParam("file") MultipartFile file) throws IOException {
        return "Uploaded: " + authService.updatePhoto(authentication.getName(), file);
    }

    @Operation(summary = "Download my photo", description = "Returns the image bytes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image returned"),
            @ApiResponse(responseCode = "404", description = "No photo uploaded")
    })
    @GetMapping("/photo")
    public ResponseEntity<Resource> downloadPhoto(Authentication authentication) {
        Resource photo = authService.getPhoto(authentication.getName());
        String name = photo.getFilename();
        MediaType type = name != null && name.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(type).body(photo);
    }
}