package com.amrita.lostandfound.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Spring securely injects your keys here when the server starts
    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true)); // Forces HTTPS for security!
    }

    public String uploadImage(MultipartFile file) throws IOException {
        // We add "folder" to keep it organized, and tell Cloudinary to auto-compress!
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "amrita-lost-and-found"
        ));

        // This takes the URL and injects the compression tags (q_auto, f_auto)
        String rawUrl = uploadResult.get("secure_url").toString();
        return rawUrl.replace("/upload/", "/upload/q_auto,f_auto/");
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return; // Nothing to delete
        }

        try {
            // Extract the Public ID from the URL
            // Example: .../upload/v123456789/my_image.jpg -> extracts "my_image"
            String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));

            // Tell Cloudinary to permanently destroy the file
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Successfully deleted image from Cloudinary: " + publicId);

        } catch (Exception e) {
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }
}