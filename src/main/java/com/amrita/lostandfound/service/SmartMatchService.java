package com.amrita.lostandfound.service;

import com.amrita.lostandfound.model.FoundItem;
import com.amrita.lostandfound.model.LostItem;
import com.amrita.lostandfound.model.MatchHistory;
import com.amrita.lostandfound.repository.FoundItemRepository;
import com.amrita.lostandfound.repository.LostItemRepository;
import com.amrita.lostandfound.repository.MatchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Service
public class SmartMatchService {

    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private FoundItemRepository foundItemRepository;
    @Autowired
    private MatchHistoryRepository matchHistoryRepository;
    @Autowired
    private TextMatchUtil textMatchUtil;
    @Autowired
    private EmailService emailService;

    // We only trigger an email if the match is 70% or higher
    private static final double MATCH_THRESHOLD = 70.0;

    // Trigger A: A student just reported a lost item
    @Async
    public void scanForLostItem(LostItem newLostItem) {
        List<FoundItem> allFoundItems = foundItemRepository.findAll();

        for (FoundItem foundItem : allFoundItems) {
            evaluateMatch(newLostItem, foundItem);
        }
    }

    // Trigger B: An Admin just uploaded a found item
    @Async
    public void scanForFoundItem(FoundItem newFoundItem) {
        List<LostItem> allLostItems = lostItemRepository.findAll();

        for (LostItem lostItem : allLostItems) {
            evaluateMatch(lostItem, newFoundItem);
        }
    }

    // The Core Logic
    // The Core Logic
    private void evaluateMatch(LostItem lostItem, FoundItem foundItem) {
        if (matchHistoryRepository.existsByLostItemIdAndFoundItemId(lostItem.getId(), foundItem.getId())) {
            return;
        }

        // 1. Score the Item Names (Worth 60% of the total score)
        double nameScore = textMatchUtil.calculateMatchPercentage(lostItem.getItemName(), foundItem.getItemName());

        // 2. Score the Details (Location + Description) (Worth 40% of the total score)
        String lostDetails = lostItem.getDescription() + " " + lostItem.getLocation();
        String foundDetails = foundItem.getDescription() + " " + foundItem.getLocation();
        double detailScore = textMatchUtil.calculateMatchPercentage(lostDetails, foundDetails);

        // 3. Calculate Final Weighted Score
        double finalScore = (nameScore * 0.6) + (detailScore * 0.4);

        // The name MUST have at least some match, and the total score must cross the threshold
        if (nameScore > 0 && finalScore >= MATCH_THRESHOLD) {

            matchHistoryRepository.save(new MatchHistory(lostItem.getId(), foundItem.getId(), finalScore));

            String message = String.format(
                    "Good news! An admin recently found a '%s' at '%s'. " +
                            "Our system thinks it might be the '%s' you lost! Please visit the Admin desk to claim it.",
                    foundItem.getItemName(), foundItem.getLocation(), lostItem.getItemName()
            );

            // Using the clean email method we just created!
            emailService.sendMatchNotification(lostItem.getStudentEmail(), message);
        }
    }
}