package ns.strategy;

import ns.model.Notification;

/**
 * @author Atithi Kumari
 * @date 12/08/26
 * @description
 */
public class SMSNotification implements SendNotification {
    @Override
    public void sendNotification(Notification notification) {
        System.out.println("Send Sms notification");
    }
}
