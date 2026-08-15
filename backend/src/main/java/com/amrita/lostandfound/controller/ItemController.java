package com.amrita.lostandfound.controller;

import com.amrita.lostandfound.model.ClaimedItem;
import com.amrita.lostandfound.model.FoundItem;
import com.amrita.lostandfound.model.LostItem;
import com.amrita.lostandfound.model.MessageResponse;
import com.amrita.lostandfound.service.SmartMatchService;
import com.amrita.lostandfound.service.CloudinaryService;
import com.amrita.lostandfound.repository.ClaimedItemRepository;
import com.amrita.lostandfound.repository.FoundItemRepository;
import com.amrita.lostandfound.repository.LostItemRepository;
import com.amrita.lostandfound.repository.UserRepository;
import com.amrita.lostandfound.service.EmailService;
import com.amrita.lostandfound.service.OtpRateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.io.IOException;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private ClaimedItemRepository claimedItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmartMatchService smartMatchService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpRateLimiterService rateLimiterService;

    private final Map<String, String> claimOtpStorage = new HashMap<>();

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    // --- FOUND ITEMS ---

    @GetMapping("/found")
    public ResponseEntity<Map<String, Object>> getFoundItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") int days,
            @RequestParam(required = false, defaultValue = "") String query) {
        
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<FoundItem> itemPage;

        LocalDateTime filterDate = null;
        if (days > 0) {
            filterDate = LocalDateTime.now().minusDays(days);
        }

        if (query != null && !query.trim().isEmpty()) {
            if (filterDate != null) {
                itemPage = foundItemRepository.findApprovedByQueryAndDate(query, filterDate, pageable);
            } else {
                itemPage = foundItemRepository.findApprovedByQuery(query, pageable);
            }
        } else {
            if (filterDate != null) {
                itemPage = foundItemRepository.findByIsApprovedTrueAndCreatedAtAfter(filterDate, pageable);
            } else {
                itemPage = foundItemRepository.findByIsApprovedTrue(pageable);
            }
        }

        itemPage.getContent().forEach(item -> {
            if (item.getReportedBy() != null) {
                userRepository.findByEmail(item.getReportedBy())
                        .ifPresent(user -> item.setReporterName(user.getName()));
            }
        });

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", itemPage.getTotalPages());
        response.put("totalItems", itemPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'FACULTY')")
    @PostMapping("/found")
    public ResponseEntity<?> reportFoundItem(
            @RequestParam("itemName") String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {

        String email = authentication.getName(); // JWT Subject is the email

        FoundItem item = new FoundItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setReportedBy(email);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        item.setIsApproved(isAdmin);

        if (image != null && !image.isEmpty()) {
            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(new MessageResponse("Image size exceeds 5MB limit", false));
            }
            String filename = org.springframework.util.StringUtils.cleanPath(image.getOriginalFilename());
            String contentType = image.getContentType();
            
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid file type. Only images allowed.", false));
            }
            
            if (!filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Only JPG/JPEG/PNG images allowed", false));
            }
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                item.setImage(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Failed to upload image to the cloud.", false));
            }
        }

        foundItemRepository.save(item);
        smartMatchService.scanForFoundItem(item);

        return ResponseEntity.ok(new MessageResponse(isAdmin ? "Found item reported successfully" : "Found item submitted for admin approval", true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") int days) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        
        LocalDateTime filterDate = null;
        if (days > 0) {
            filterDate = LocalDateTime.now().minusDays(days);
        }

        Page<FoundItem> itemPage;
        if (filterDate != null) {
            itemPage = foundItemRepository.findByIsApprovedFalseAndCreatedAtAfter(filterDate, pageable);
        } else {
            itemPage = foundItemRepository.findByIsApprovedFalse(pageable);
        }

        itemPage.getContent().forEach(item -> {
            if (item.getReportedBy() != null) {
                userRepository.findByEmail(item.getReportedBy())
                        .ifPresent(user -> item.setReporterName(user.getName()));
            }
        });

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", itemPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveItem(@PathVariable Long id) {
        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            FoundItem item = itemOpt.get();
            item.setIsApproved(true);
            foundItemRepository.save(item);
            return ResponseEntity.ok(new MessageResponse("Item approved successfully", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Item not found", false));
    }

    @DeleteMapping("/found/{id}")
    public ResponseEntity<?> deleteFoundItem(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String email = authentication != null ? authentication.getName() : null;

        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            FoundItem item = itemOpt.get();
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Only admins can delete found items", false));
            }

            if (item.getImage() != null && !item.getImage().isEmpty()) {
                cloudinaryService.deleteImage(item.getImage());
            }
            foundItemRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("Item deleted successfully", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Item not found", false));
    }

    // --- LOST ITEMS ---

    @GetMapping("/lost")
    public ResponseEntity<Map<String, Object>> getLostItems(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(required = false, defaultValue = "0") int days,
            Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        
        LocalDateTime filterDate = null;
        if (days > 0) {
            filterDate = LocalDateTime.now().minusDays(days);
        }

        Page<LostItem> itemPage;

        if (isAdmin) {
            if (filterDate != null) {
                itemPage = lostItemRepository.findAllByCreatedAtAfter(filterDate, pageable);
            } else {
                itemPage = lostItemRepository.findAll(pageable);
            }
        } else {
            String email = authentication.getName();
            if (filterDate != null) {
                itemPage = lostItemRepository.findByStudentEmailAndCreatedAtAfter(email, filterDate, pageable);
            } else {
                itemPage = lostItemRepository.findByStudentEmail(email, pageable);
            }
        }

        itemPage.getContent().forEach(item -> {
            if (item.getStudentEmail() != null) {
                userRepository.findByEmail(item.getStudentEmail())
                        .ifPresent(user -> item.setReporterName(user.getName()));
            }
        });

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", itemPage.getTotalPages());
        response.put("totalItems", itemPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    @PostMapping("/lost")
    public ResponseEntity<?> reportLostItem(
            @RequestParam("itemName") String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
            
        String email = authentication.getName();
        
        LostItem lostItem = new LostItem();
        lostItem.setStudentEmail(email);
        lostItem.setItemName(itemName);
        lostItem.setDescription(description);
        lostItem.setLocation(location);
        
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(new MessageResponse("Image size exceeds 5MB limit", false));
            }
            String filename = org.springframework.util.StringUtils.cleanPath(image.getOriginalFilename());
            String contentType = image.getContentType();
            
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid file type. Only images allowed.", false));
            }
            
            if (!filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Only JPG/JPEG/PNG images allowed", false));
            }
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                lostItem.setImage(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Failed to upload image to the cloud.", false));
            }
        }
        
        lostItemRepository.save(lostItem);
        smartMatchService.scanForLostItem(lostItem);

        return ResponseEntity.ok(new MessageResponse("Lost item reported successfully", true));
    }

    @DeleteMapping("/lost/{id}")
    public ResponseEntity<?> deleteLostItem(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String email = authentication != null ? authentication.getName() : null;

        Optional<LostItem> itemOpt = lostItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            LostItem item = itemOpt.get();
            if (!isAdmin && (email == null || !email.equals(item.getStudentEmail()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Not authorized to delete this item", false));
            }
            lostItemRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("Lost item record deleted", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Lost item not found", false));
    }

    // --- MY FOUND ITEMS ---
    @GetMapping("/my-found")
    public ResponseEntity<Map<String, Object>> getMyFoundItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") int days,
            Authentication authentication) {
        
        String email = authentication.getName();
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        
        LocalDateTime filterDate = null;
        if (days > 0) {
            filterDate = LocalDateTime.now().minusDays(days);
        }

        Page<FoundItem> itemPage;
        if (filterDate != null) {
            itemPage = foundItemRepository.findByReportedByAndCreatedAtAfter(email, filterDate, pageable);
        } else {
            itemPage = foundItemRepository.findByReportedBy(email, pageable);
        }

        itemPage.getContent().forEach(item -> {
            userRepository.findByEmail(item.getReportedBy())
                    .ifPresent(user -> item.setReporterName(user.getName()));
        });

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", itemPage.getTotalPages());
        response.put("totalItems", itemPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // --- CLAIM ITEMS ---

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/claim/initiate/{id}")
    public ResponseEntity<?> initiateClaim(
            @PathVariable Long id,
            @RequestBody Map<String, String> claimData) {
        
        String registrationNumber = claimData.get("registration_number");
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Registration number is required", false));
        }

        String studentEmail = registrationNumber.toLowerCase().trim() + "@bl.students.amrita.edu";

        io.github.bucket4j.Bucket bucket = rateLimiterService.resolveBucket(studentEmail);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many OTP requests. Try again later.", false));
        }

        String otp = generateOtp();
        
        claimOtpStorage.put(registrationNumber, otp);
        emailService.sendOtp(studentEmail, otp);
        
        return ResponseEntity.ok(new MessageResponse("OTP sent to " + studentEmail, true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/claim/{id}")
    public ResponseEntity<?> claimItem(
            @PathVariable Long id,
            @RequestBody Map<String, String> claimData) {

        String studentName = claimData.get("student_name");
        String registrationNumber = claimData.get("registration_number");
        String otp = claimData.get("otp");

        if (otp == null || !otp.equals(claimOtpStorage.get(registrationNumber))) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired OTP", false));
        }

        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            FoundItem foundItem = itemOpt.get();

            ClaimedItem claimedItem = new ClaimedItem();
            claimedItem.setItemName(foundItem.getItemName());
            claimedItem.setDescription(foundItem.getDescription());
            claimedItem.setLocation(foundItem.getLocation());
            claimedItem.setImage(foundItem.getImage());
            claimedItem.setStudentName(studentName);
            claimedItem.setRegistrationNumber(registrationNumber);
            claimedItem.setClaimedAt(LocalDateTime.now());

            claimedItemRepository.save(claimedItem);
            foundItemRepository.delete(foundItem);
            
            claimOtpStorage.remove(registrationNumber);

            return ResponseEntity.ok(new MessageResponse("Item claimed successfully", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Item not found", false));
    }

    @GetMapping("/claimed")
    public ResponseEntity<Map<String, Object>> getClaimedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") int days) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        LocalDateTime filterDate = null;
        if (days > 0) {
            filterDate = LocalDateTime.now().minusDays(days);
        }

        Page<ClaimedItem> itemPage;
        if (filterDate != null) {
            itemPage = claimedItemRepository.findAllByClaimedAtAfterOrderByClaimedAtDesc(filterDate, pageable);
        } else {
            itemPage = claimedItemRepository.findAllByOrderByClaimedAtDesc(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", itemPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/claimed/{id}")
    public ResponseEntity<?> deleteClaimedItem(@PathVariable Long id) {
        Optional<ClaimedItem> itemOpt = claimedItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            ClaimedItem item = itemOpt.get();
            if (item.getImage() != null && !item.getImage().isEmpty()) {
                cloudinaryService.deleteImage(item.getImage());
            }
            claimedItemRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("Claimed record deleted", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Record not found", false));
    }
}