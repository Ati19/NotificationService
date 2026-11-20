package notificationservice.strategy;

import notificationservice.model.PartnerShare;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Picks a partner for each message so a tenant's traffic to a country follows its percentage split.
 * Uses smooth weighted round-robin: with Airtel 70 / Jio 30, every 10 messages go exactly
 * 7 to Airtel and 3 to Jio, interleaved (A J A A A J A A J A) rather than in bursts.
 */
public class PartnerSelector {
    private final Map<String, WeightedRoundRobin> stateByKey = new ConcurrentHashMap<>();

    public String select(String key, List<PartnerShare> shares) {
        // A different split (tenant changed it) starts a fresh rotation. Compared by value so that
        // updating one country's split, which rebuilds the whole config, doesn't reset the others.
        WeightedRoundRobin wrr = stateByKey.compute(key,
                (k, current) -> current != null && current.shares.equals(shares) ? current : new WeightedRoundRobin(shares));
        return wrr.next();
    }

    private static class WeightedRoundRobin {
        private final List<PartnerShare> shares;
        private final int[] currentWeights;
        private final int totalWeight;

        WeightedRoundRobin(List<PartnerShare> shares) {
            this.shares = shares;
            this.currentWeights = new int[shares.size()];
            this.totalWeight = shares.stream().mapToInt(PartnerShare::getPercentage).sum();
        }

        synchronized String next() {
            int best = 0;
            for (int i = 0; i < shares.size(); i++) {
                currentWeights[i] += shares.get(i).getPercentage();
                if (currentWeights[i] > currentWeights[best]) {
                    best = i;
                }
            }
            currentWeights[best] -= totalWeight;
            return shares.get(best).getPartnerName();
        }
    }
}
