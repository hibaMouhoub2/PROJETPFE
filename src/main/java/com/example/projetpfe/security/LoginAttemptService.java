package com.example.projetpfe.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 5;
    private final int BLOCK_DURATION_MINUTES = 30; // Durée de blocage en minutes
    private final ConcurrentMap<String, BlockData> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = new ConcurrentHashMap<>();
    }

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(String key) {
        BlockData blockData = attemptsCache.getOrDefault(key, new BlockData(0, false, 0));

        // Si le temps de blocage est expiré, réinitialiser
        if (blockData.isBlocked() && isBlockExpired(blockData)) {
            blockData = new BlockData(0, false, 0);
        }

        // Incrémenter le nombre de tentatives
        int attempts = blockData.getAttempts() + 1;
        boolean blocked = attempts >= MAX_ATTEMPT;
        long blockTime = blocked ? System.currentTimeMillis() : 0;

        attemptsCache.put(key, new BlockData(attempts, blocked, blockTime));
    }

    public boolean isBlocked(String key) {
        BlockData blockData = attemptsCache.get(key);
        if (blockData == null) {
            return false;
        }

        if (blockData.isBlocked()) {
            // Vérifier si le temps de blocage est expiré
            if (isBlockExpired(blockData)) {
                // Réinitialiser le blocage
                attemptsCache.put(key, new BlockData(0, false, 0));
                return false;
            }
            return true;
        }

        return false;
    }

    public long getRemainingBlockTimeInMinutes(String key) {
        BlockData blockData = attemptsCache.get(key);
        if (blockData != null && blockData.isBlocked()) {
            long elapsedTimeMs = System.currentTimeMillis() - blockData.getBlockTime();
            long blockDurationMs = TimeUnit.MINUTES.toMillis(BLOCK_DURATION_MINUTES);
            long remainingTimeMs = blockDurationMs - elapsedTimeMs;

            if (remainingTimeMs > 0) {
                return TimeUnit.MILLISECONDS.toMinutes(remainingTimeMs) + 1; // Arrondir à la minute supérieure
            }
        }
        return 0;
    }

    private boolean isBlockExpired(BlockData blockData) {
        if (!blockData.isBlocked()) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long blockTime = blockData.getBlockTime();
        long blockDurationMs = TimeUnit.MINUTES.toMillis(BLOCK_DURATION_MINUTES);

        return (currentTime - blockTime) > blockDurationMs;
    }

    // Classe interne pour stocker les données de blocage
    private static class BlockData {
        private final int attempts;
        private final boolean blocked;
        private final long blockTime; // Timestamp du moment où le blocage a commencé

        public BlockData(int attempts, boolean blocked, long blockTime) {
            this.attempts = attempts;
            this.blocked = blocked;
            this.blockTime = blockTime;
        }

        public int getAttempts() {
            return attempts;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public long getBlockTime() {
            return blockTime;
        }
    }
}