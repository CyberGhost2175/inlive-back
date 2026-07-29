package ai.lab.inlive.services.push;

import ai.lab.inlive.entities.enums.ClientResponseStatus;
import ai.lab.inlive.entities.enums.PriceRequestStatus;
import ai.lab.inlive.entities.enums.PushEntityType;
import ai.lab.inlive.entities.enums.PushNotificationType;
import ai.lab.inlive.entities.enums.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class PushMessageFactory {

    public PushPayload priceOfferCreated(Long priceRequestId, PriceRequestStatus status) {
        return PushPayload.builder()
                .type(PushNotificationType.PRICE_REQUEST_UPDATED)
                .entityId(String.valueOf(priceRequestId))
                .entityType(PushEntityType.PRICE_REQUEST)
                .newStatus(status.name())
                .title("Новое предложение цены")
                .body("Менеджер отправил вам предложение по цене")
                .build();
    }

    public PushPayload priceOfferUpdated(Long priceRequestId, PriceRequestStatus status) {
        String body = switch (status) {
            case RAISED -> "Менеджер повысил предложенную цену";
            case DECREASED -> "Менеджер снизил предложенную цену";
            case ACCEPTED -> "Менеджер обновил предложенную цену";
        };
        return PushPayload.builder()
                .type(PushNotificationType.PRICE_REQUEST_UPDATED)
                .entityId(String.valueOf(priceRequestId))
                .entityType(PushEntityType.PRICE_REQUEST)
                .newStatus(status.name())
                .title("Обновление предложения цены")
                .body(body)
                .build();
    }

    public PushPayload priceRequestHidden(Long priceRequestId) {
        return PushPayload.builder()
                .type(PushNotificationType.PRICE_REQUEST_UPDATED)
                .entityId(String.valueOf(priceRequestId))
                .entityType(PushEntityType.PRICE_REQUEST)
                .newStatus("HIDDEN")
                .title("Предложение скрыто")
                .body("Менеджер скрыл предложение цены")
                .build();
    }

    public PushPayload clientRespondedToPrice(Long priceRequestId, ClientResponseStatus clientResponse) {
        boolean accepted = clientResponse == ClientResponseStatus.ACCEPTED;
        return PushPayload.builder()
                .type(PushNotificationType.PRICE_REQUEST_UPDATED)
                .entityId(String.valueOf(priceRequestId))
                .entityType(PushEntityType.PRICE_REQUEST)
                .newStatus(clientResponse.name())
                .title(accepted ? "Клиент принял цену" : "Клиент отклонил цену")
                .body(accepted
                        ? "Клиент принял предложенную цену"
                        : "Клиент отклонил предложенную цену")
                .build();
    }

    public PushPayload bookingCreatedForClient(Long reservationId) {
        return PushPayload.builder()
                .type(PushNotificationType.BOOKING_CREATED)
                .entityId(String.valueOf(reservationId))
                .entityType(PushEntityType.BOOKING)
                .newStatus(ReservationStatus.WAITING_TO_APPROVE.name())
                .title("Бронь создана")
                .body("Создана бронь со статусом «ожидает подтверждения»")
                .build();
    }

    public PushPayload bookingWaitingToApprove(Long reservationId) {
        return PushPayload.builder()
                .type(PushNotificationType.BOOKING_CREATED)
                .entityId(String.valueOf(reservationId))
                .entityType(PushEntityType.BOOKING)
                .newStatus(ReservationStatus.WAITING_TO_APPROVE.name())
                .title("Новая бронь")
                .body("Новая бронь ожидает вашего подтверждения")
                .build();
    }

    public PushPayload bookingStatusChanged(Long reservationId, ReservationStatus status) {
        String title;
        String body;
        switch (status) {
            case APPROVED -> {
                title = "Бронь подтверждена";
                body = "Менеджер подтвердил вашу бронь";
            }
            case REJECTED -> {
                title = "Бронь отклонена";
                body = "Менеджер отклонил вашу бронь";
            }
            case FINISHED_SUCCESSFUL, SUCCESSFUL -> {
                title = "Бронь завершена";
                body = "Бронь успешно завершена";
            }
            case CLIENT_DIDNT_CAME -> {
                title = "Клиент не явился";
                body = "Менеджер отметил, что клиент не явился";
            }
            default -> {
                title = "Статус брони изменён";
                body = "Статус вашей брони обновлён: " + status.name();
            }
        }
        return PushPayload.builder()
                .type(PushNotificationType.BOOKING_STATUS_CHANGED)
                .entityId(String.valueOf(reservationId))
                .entityType(PushEntityType.BOOKING)
                .newStatus(status.name())
                .title(title)
                .body(body)
                .build();
    }

    public PushPayload bookingCanceled(Long reservationId) {
        return PushPayload.builder()
                .type(PushNotificationType.BOOKING_CANCELED)
                .entityId(String.valueOf(reservationId))
                .entityType(PushEntityType.BOOKING)
                .newStatus(ReservationStatus.CANCELED.name())
                .title("Бронь отменена")
                .body("Клиент отменил бронирование")
                .build();
    }

    public PushPayload newRelevantSearchRequest(Long searchRequestId) {
        return PushPayload.builder()
                .type(PushNotificationType.NEW_PRICE_REQUEST)
                .entityId(String.valueOf(searchRequestId))
                .entityType(PushEntityType.PRICE_REQUEST)
                .newStatus("OPEN_TO_PRICE_REQUEST")
                .title("Новая заявка")
                .body("Появилась заявка, подходящая вашему объекту")
                .build();
    }
}
