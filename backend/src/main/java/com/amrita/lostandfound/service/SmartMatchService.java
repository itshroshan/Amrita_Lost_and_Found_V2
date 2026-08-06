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

        FoundItem bestMatch = null;
        double maxScore = -1;

        for (FoundItem foundItem : allFoundItems) {
            if (matchHistoryRepository.existsByLostItemIdAndFoundItemId(newLostItem.getId(), foundItem.getId())) {
                continue; // Already emailed about this pair
            }

            double score = getMatchScore(newLostItem, foundItem);
            if (score > maxScore) {
                maxScore = score;
                bestMatch = foundItem;
            }
        }

        // Only trigger an email for the absolute best match that passes the threshold
        if (bestMatch != null && maxScore >= MATCH_THRESHOLD) {
            triggerMatchEmail(newLostItem, bestMatch, maxScore);
        }
    }

    // Trigger B: An Admin just uploaded a found item
    @Async
    public void scanForFoundItem(FoundItem newFoundItem) {
        List<LostItem> allLostItems = lostItemRepository.findAll();

        LostItem bestMatch = null;
        double maxScore = -1;

        for (LostItem lostItem : allLostItems) {
            if (matchHistoryRepository.existsByLostItemIdAndFoundItemId(lostItem.getId(), newFoundItem.getId())) {
                continue;
            }

            double score = getMatchScore(lostItem, newFoundItem);
            if (score > maxScore) {
                maxScore = score;
                bestMatch = lostItem;
            }
        }

        // Only notify the one student whose lost item is the absolute best match
        if (bestMatch != null && maxScore >= MATCH_THRESHOLD) {
            triggerMatchEmail(bestMatch, newFoundItem, maxScore);
        }
    }

    // Helper: Calculate the score without triggering anything
    private double getMatchScore(LostItem lostItem, FoundItem foundItem) {
        // 1. Score the Item Names (Worth 60% of the total score)
        double nameScore = textMatchUtil.calculateMatchPercentage(lostItem.getItemName(), foundItem.getItemName());
        
        // The name MUST have at least some match
        if (nameScore == 0) return 0.0;

        // 2. Score the Details (Location + Description) (Worth 40% of the total score)
        String lostDetails = lostItem.getDescription() + " " + lostItem.getLocation();
        String foundDetails = foundItem.getDescription() + " " + foundItem.getLocation();
        double detailScore = textMatchUtil.calculateMatchPercentage(lostDetails, foundDetails);

        // 3. Calculate Final Weighted Score
        return (nameScore * 0.6) + (detailScore * 0.4);
    }

    // Helper: Trigger the actual email and save to history
    private void triggerMatchEmail(LostItem lostItem, FoundItem foundItem, double score) {
        matchHistoryRepository.save(new MatchHistory(lostItem.getId(), foundItem.getId(), score));

        String message = String.format(
                "Good news! An admin recently found a '%s' at '%s'. " +
                        "Our system considers it a highly likely match for the '%s' you lost! Please visit the Admin desk to claim it.",
                foundItem.getItemName(), foundItem.getLocation(), lostItem.getItemName()
        );

        emailService.sendMatchNotification(lostItem.getStudentEmail(), message);
    }
}