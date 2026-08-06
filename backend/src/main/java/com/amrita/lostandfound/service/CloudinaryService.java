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
    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
        this.cloudinary.config.secure = true; // Forces HTTPS for security!
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
            return;
        }

        try {
            String publicId = "";

            // 1. Check if the image is in our specific folder
            if (imageUrl.contains("amrita-lost-and-found/")) {
                // Grabs everything from the start of the folder name to the dot before the extension
                // Example: extracts "amrita-lost-and-found/my_image"
                publicId = imageUrl.substring(imageUrl.indexOf("amrita-lost-and-found/"), imageUrl.lastIndexOf("."));
            } else {
                // Fallback just in case you have older images uploaded before we made the folder
                publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
            }

            // 2. Tell Cloudinary to destroy it and CAPTURE the response
            Map result = cloudinary.uploader().destroy(publicId, com.cloudinary.utils.ObjectUtils.emptyMap());

            // 3. Print the actual API response (It should print "ok" if successful, or "not found" if it missed)
            System.out.println("Cloudinary delete result for [" + publicId + "]: " + result.get("result"));

        } catch (Exception e) {
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }
}