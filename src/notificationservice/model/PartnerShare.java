package notificationservice.model;

/**
 * One partner (Airtel, Jio, ...) and the percentage of a tenant's traffic it should get.
 */
public class PartnerShare {
    private final String partnerName;
    private final int percentage;

    public PartnerShare(String partnerName, int percentage) {
        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be 1-100, got " + percentage + " for " + partnerName);
        }
        this.partnerName = partnerName;
        this.percentage = percentage;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public int getPercentage() {
        return percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PartnerShare)) {
            return false;
        }
        PartnerShare other = (PartnerShare) o;
        return percentage == other.percentage && partnerName.equals(other.partnerName);
    }

    @Override
    public int hashCode() {
        return 31 * partnerName.hashCode() + percentage;
    }

    @Override
    public String toString() {
        return partnerName + " " + percentage + "%";
    }
}
