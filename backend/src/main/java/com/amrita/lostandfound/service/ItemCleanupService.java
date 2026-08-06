package com.amrita.lostandfound.service;

import com.amrita.lostandfound.repository.ClaimedItemRepository;
import com.amrita.lostandfound.repository.FoundItemRepository;
import com.amrita.lostandfound.repository.LostItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ItemCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(ItemCleanupService.class);

    @Autowired
    private ClaimedItemRepository claimedItemRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    /**
     * Runs every day at midnight (00:00).
     * Deletes Claimed, Found, and Lost items that are older than 60 days.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldItems() {
        logger.info("Starting scheduled cleanup of items older than 60 days...");
        
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(60);

        try {
            claimedItemRepository.deleteByClaimedAtBefore(thresholdDate);
            logger.info("Successfully cleaned up old Claimed Items.");
            
            foundItemRepository.deleteByCreatedAtBefore(thresholdDate);
            logger.info("Successfully cleaned up old Found Items.");
            
            lostItemRepository.deleteByCreatedAtBefore(thresholdDate);
            logger.info("Successfully cleaned up old Lost Items.");
            
        } catch (Exception e) {
            logger.error("Error occurred during item cleanup: ", e);
        }
        
        logger.info("Finished scheduled cleanup.");
    }
}
