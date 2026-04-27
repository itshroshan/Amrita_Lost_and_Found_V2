package com.amrita.lostandfound.controller;

import com.amrita.lostandfound.model.ClaimedItem;
import com.amrita.lostandfound.model.FoundItem;
import com.amrita.lostandfound.model.LostItem;
import com.amrita.lostandfound.service.SmartMatchService;
import com.amrita.lostandfound.repository.ClaimedItemRepository;
import com.amrita.lostandfound.repository.FoundItemRepository;
import com.amrita.lostandfound.repository.LostItemRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

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
                                 HttpSession session, Model model) throws IOException {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";

        String filename = StringUtils.cleanPath(image.getOriginalFilename());
        if (!filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
            model.addAttribute("error", "Only JPG/JPEG/PNG images allowed");
            return "upload_item";
        }

        Files.createDirectories(Paths.get(UPLOAD_DIR));
        Path path = Paths.get(UPLOAD_DIR + filename);
        image.transferTo(new File(path.toAbsolutePath().toString()));

        FoundItem item = new FoundItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setImage(filename);
        item.setReportedBy("Admin");
        foundItemRepository.save(item);

        // Trigger the Smart Match engine to scan lost items!
        smartMatchService.scanForFoundItem(item);

        return "redirect:/admin";
    }

    @GetMapping("/view-items")
    public String viewItems(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("items", foundItemRepository.findAll());
        return "view_items"; // view_items.html
    }

    @GetMapping("/delete-item/{id}")
    public String deleteItem(@PathVariable Long id, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";

        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            String imageFilename = itemOpt.get().getImage();
            File imageFile = new File(UPLOAD_DIR + imageFilename);
            if (imageFile.exists()) {
                imageFile.delete();
            }
            foundItemRepository.deleteById(id);
        }
        return "redirect:/view-items";
    }

    @GetMapping("/view-lost-items")
    public String viewLostItems(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("items", lostItemRepository.findAll());
        return "view_lost_items"; // view_lost_item.html
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
    public String claimedItems(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        // Assuming your repository has a method to sort, otherwise findAll() works too
        model.addAttribute("items", claimedItemRepository.findAllByOrderByClaimedAtDesc());
        return "claimed_items"; // claimed_items.html
    }

    @GetMapping("/delete-claimed-item/{id}")
    public String deleteClaimedItem(@PathVariable Long id, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        claimedItemRepository.deleteById(id);
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
                              HttpSession session, Model model) {
        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";

        List<FoundItem> allItems = foundItemRepository.findAll();
        List<FoundItem> searchResults;

        if (query != null && !query.trim().isEmpty()) {
            // Using a custom repository method or simple stream filtering. Assuming custom method exists:
            searchResults = foundItemRepository.findByItemNameContainingIgnoreCaseOrLocationContainingIgnoreCase(query, query);
        } else {
            searchResults = List.of(); // Empty list if no query
        }

        model.addAttribute("query", query);
        model.addAttribute("search_results", searchResults);
        model.addAttribute("all_items", allItems);
        return "search_items"; // search_items.html
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
                                  HttpSession session, Model model) throws IOException {
        if (!"student".equals(session.getAttribute("role"))) return "redirect:/";

        String filename = StringUtils.cleanPath(image.getOriginalFilename());
        if (!filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
            model.addAttribute("error", "Only JPG/JPEG/PNG images allowed");
            return "report_found";
        }

        Files.createDirectories(Paths.get(UPLOAD_DIR));
        Path path = Paths.get(UPLOAD_DIR + filename);
        image.transferTo(new File(path.toAbsolutePath().toString()));

        FoundItem item = new FoundItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setImage(filename);
        item.setReportedBy((String) session.getAttribute("email"));
        foundItemRepository.save(item);

        // Trigger the Smart Match engine to scan lost items!
        smartMatchService.scanForFoundItem(item);

        return "redirect:/student";
    }
}