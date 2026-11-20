package ns.factory;


import ns.model.NotificationType;
import ns.strategy.*;

/**
 * @author Atithi Kumari
 * @date 12/08/26
 * @description
 */
public class NotificationStrategyFactory {
    public static SendNotification createNotification(NotificationType type){

     return switch (type){
         case SMS -> new SMSNotification();
         case PUSH -> new PushNotification();
         case EMAIL -> new EmailNotification();
         case WHATSAPP -> new WhatsappNotification();
        };
    }
}
