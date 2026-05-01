package com.amrita.lostandfound.controller;

import com.amrita.lostandfound.model.ClaimedItem;
import com.amrita.lostandfound.model.FoundItem;
import com.amrita.lostandfound.model.LostItem;
import com.amrita.lostandfound.service.SmartMatchService;
import com.amrita.lostandfound.service.CloudinaryService;
import com.amrita.lostandfound.repository.ClaimedItemRepository;
import com.amrita.lostandfound.repository.FoundItemRepository;
import com.amrita.lostandfound.repository.LostItemRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.security.Principal;

@Controller
public class ItemController {

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private ClaimedItemRepository claimedItemRepository;

    @Autowired
    private SmartMatchService smartMatchService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // --- ADMIN ROUTES ---
    @GetMapping("/admin")
    public String adminDashboard(HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        return "admin_dashboard"; // admin_dashboard.html
    }

    @GetMapping("/upload-item")
    public String uploadItemPage(HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        return "upload_item"; // upload_item.html
    }

    @PostMapping("/upload-item")
    public String uploadItemPost(@RequestParam String itemName,
                                 @RequestParam String description,
                                 @RequestParam String location,
                                 @RequestParam("image") MultipartFile image,
                                 Model model) { // Notice we deleted HttpSession!

        // 1. Create the item
        FoundItem item = new FoundItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setReportedBy("Admin");

        // 2. Handle the Cloud Upload
        if (!image.isEmpty()) {
            String filename = org.springframework.util.StringUtils.cleanPath(image.getOriginalFilename());

            // Optional: Keep your original file extension validation
            if (!filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                model.addAttribute("error", "Only JPG/JPEG/PNG images allowed");
                return "upload_item";
            }

            try {
                // Send physical file to Cloudinary, get a secure web URL back
                String imageUrl = cloudinaryService.uploadImage(image);

                // Save the URL to your entity (Use setImageUrl() if you renamed the field!)
                item.setImage(imageUrl);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Failed to upload image to the cloud.");
                return "upload_item";
            }
        }

        // 3. Save text details + image URL to PostgreSQL
        foundItemRepository.save(item);

        // 4. Trigger the background Smart Match engine
        smartMatchService.scanForFoundItem(item);

        return "redirect:/admin";
    }

    @GetMapping("/view-items")
    public String viewItems(@RequestParam(defaultValue = "0") int page, Model model) {

        // 1. Set how many items you want to display per page
        int pageSize = 2;

        // 2. Build the pagination request
        Pageable pageable = PageRequest.of(page, pageSize);

        // 3. Ask PostgreSQL for ONLY this specific page of data
        Page<FoundItem> itemPage = foundItemRepository.findAll(pageable);

        // 4. Send the data and the page numbers to the HTML template
        model.addAttribute("items", itemPage.getContent()); // Gets just the list of items for this page
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());

        return "view_items";
    }

    @GetMapping("/delete-item/{id}")
    public String deleteItem(@PathVariable Long id) { // We deleted HttpSession!

        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);

        if (itemOpt.isPresent()) {
            FoundItem item = itemOpt.get();

            // 1. Assassinate the image in the cloud
            // We check if it's not null just in case an item was uploaded without an image
            if (item.getImage() != null && !item.getImage().isEmpty()) {
                cloudinaryService.deleteImage(item.getImage());
            }

            // 2. Erase the record from the PostgreSQL database
            foundItemRepository.deleteById(id);
        }

        return "redirect:/view-items";
    }

    @GetMapping("/view-lost-items")
    public String viewLostItems(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";

        // Increased to 10 since there are no images weighing the page down!
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        // JpaRepository has this built-in, no custom repository code needed
        Page<LostItem> itemPage = lostItemRepository.findAll(pageable);

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());

        return "view_lost_items";
    }

    @GetMapping("/delete-lost-item/{id}")
    public String deleteLostItem(@PathVariable Long id, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        lostItemRepository.deleteById(id);
        return "redirect:/view-lost-items";
    }

    @GetMapping("/claim-item/{id}")
    public String claimItemPage(@PathVariable Long id, HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";

        if (foundItemRepository.findById(id).isEmpty()) return "redirect:/view-items";
        model.addAttribute("item_id", id);
        return "claim_item"; // claim_item.html
    }

    @PostMapping("/claim-item/{id}")
    public String claimItemPost(@PathVariable Long id,
                                @RequestParam String student_name,
                                @RequestParam String registration_number,
                                HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";

        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            FoundItem foundItem = itemOpt.get();

            ClaimedItem claimedItem = new ClaimedItem();
            claimedItem.setItemName(foundItem.getItemName());
            claimedItem.setDescription(foundItem.getDescription());
            claimedItem.setLocation(foundItem.getLocation());
            claimedItem.setImage(foundItem.getImage());
            claimedItem.setStudentName(student_name);
            claimedItem.setRegistrationNumber(registration_number);
            claimedItem.setClaimedAt(LocalDateTime.now());

            claimedItemRepository.save(claimedItem);
            foundItemRepository.deleteById(id);
        }
        return "redirect:/view-items";
    }

    @GetMapping("/claimed-items")
    public String claimedItems(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";

        int pageSize = 2;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Fetch the sorted AND paginated data
        Page<ClaimedItem> itemPage = claimedItemRepository.findAllByOrderByClaimedAtDesc(pageable);

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());

        return "claimed_items";
    }

    @GetMapping("/delete-claimed-item/{id}")
    public String deleteClaimedItem(@PathVariable Long id) { // HttpSession removed!

        // 1. Fetch the item first so we can grab the Cloudinary URL
        Optional<ClaimedItem> itemOpt = claimedItemRepository.findById(id);

        if (itemOpt.isPresent()) {
            ClaimedItem item = itemOpt.get();

            // 2. Assassinate the image in the cloud
            if (item.getImage() != null && !item.getImage().isEmpty()) {
                cloudinaryService.deleteImage(item.getImage());
            }

            // 3. Erase the record from the PostgreSQL database
            claimedItemRepository.deleteById(id);
        }

        return "redirect:/claimed-items";
    }

    // --- STUDENT ROUTES ---
    @GetMapping("/student")
    public String studentDashboard(HttpSession session, Model model) {
        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("student_name", session.getAttribute("name"));
        return "student_dashboard"; // student_dashboard.html
    }

    @GetMapping("/search")
    public String searchItems(@RequestParam(required = false, defaultValue = "") String query,
                              @RequestParam(defaultValue = "0") int page,
                              HttpSession session, Model model) {

        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";

        int pageSize = 2; // Set how many search results per page
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FoundItem> itemPage;

        // If they typed something, search for it using the Pageable repository method
        if (query != null && !query.trim().isEmpty()) {
            itemPage = foundItemRepository.findByItemNameContainingIgnoreCaseOrLocationContainingIgnoreCase(query, query, pageable);
        }
        // If the search bar is empty, just load the regular paginated list
        else {
            itemPage = foundItemRepository.findAll(pageable);
        }

        // Pass everything to the HTML
        model.addAttribute("query", query);
        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());

        return "search_items";
    }

    @GetMapping("/report-lost")
    public String reportLostPage(HttpSession session) {
        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";
        return "report_lost"; // report_lost.html
    }

    @PostMapping("/report-lost")
    public String reportLostPost(@RequestParam String itemName,
                                 @RequestParam String description,
                                 @RequestParam String location,
                                 HttpSession session) {
        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";

        LostItem lostItem = new LostItem();
        lostItem.setStudentEmail((String) session.getAttribute("email"));
        lostItem.setItemName(itemName);
        lostItem.setDescription(description);
        lostItem.setLocation(location);
        lostItemRepository.save(lostItem);

        // Trigger the Smart Match engine to scan found items!
        smartMatchService.scanForLostItem(lostItem);

        return "redirect:/student";
    }

    @GetMapping("/report-found")
    public String reportFoundPage(HttpSession session) {
        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";
        return "report_found"; // report_found.html
    }

    @PostMapping("/report-found")
    public String reportFoundPost(@RequestParam String itemName,
                                  @RequestParam String description,
                                  @RequestParam String location,
                                  @RequestParam("image") MultipartFile image,
                                  HttpSession session, // <-- SWAPPED: Using HttpSession instead of Principal!
                                  Model model) {

        // 1. Grab the user's email from the session to fix the NullPointerException
        String uploaderEmail = (String) session.getAttribute("email");

        // Safety check: If they somehow bypassed the login screen, kick them back to login
        if (uploaderEmail == null) {
            return "redirect:/login";
        }

        FoundItem item = new FoundItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);

        // 2. Attach the logged-in student's email to the item
        // (Note: If your entity uses "setUploadedBy" instead of "setReportedBy", change this here!)
        item.setReportedBy(uploaderEmail);

        // 3. Handle the Cloudinary Upload
        if (!image.isEmpty()) {
            String filename = org.springframework.util.StringUtils.cleanPath(image.getOriginalFilename());

            if (!filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                model.addAttribute("error", "Only JPG/JPEG/PNG images allowed");
                return "report_found";
            }

            try {
                // Send to Cloudinary (Your Thumbnailator service squishes it first!)
                String imageUrl = cloudinaryService.uploadImage(image);
                item.setImage(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Failed to upload image to the cloud.");
                return "report_found";
            }
        }

        // 4. Save the text data to PostgreSQL
        foundItemRepository.save(item);

        // 5. Trigger the Smart Match engine
        // (Because you added @Async, this runs instantly in the background without freezing the webpage!)
        smartMatchService.scanForFoundItem(item);

        return "redirect:/student";
    }
}