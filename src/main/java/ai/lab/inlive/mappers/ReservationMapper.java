package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.entities.Reservation;
import ai.lab.inlive.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "clientId", source = "approvedBy.id")
    @Mapping(target = "clientName", expression = "java(getClientName(reservation))")
    @Mapping(target = "clientFirstName", expression = "java(getClientFirstName(reservation))")
    @Mapping(target = "clientLastName", expression = "java(getClientLastName(reservation))")
    @Mapping(target = "clientPhoneNumber", expression = "java(getClientPhoneNumber(reservation))")
    @Mapping(target = "accommodationUnitId", source = "unit.id")
    @Mapping(target = "accommodationUnitName", source = "unit.name")
    @Mapping(target = "accommodationName", source = "unit.accommodation.name")
    @Mapping(target = "city", source = "unit.accommodation.city.name")
    @Mapping(target = "district", source = "unit.accommodation.district.name")
    @Mapping(target = "address", source = "unit.accommodation.address")
    @Mapping(target = "priceRequestId", source = "priceRequest.id")
    @Mapping(target = "searchRequestId", source = "searchRequest.id")
    @Mapping(target = "price", source = "priceRequest.price")
    @Mapping(target = "status", expression = "java(reservation.getStatus() != null ? reservation.getStatus().name() : null)")
    @Mapping(target = "checkInDate", source = "searchRequest.fromDate")
    @Mapping(target = "checkOutDate", source = "searchRequest.toDate")
    @Mapping(target = "guestCount", source = "searchRequest.countOfPeople")
    @Mapping(target = "managerPhoneNumber", expression = "java(getManagerPhoneNumber(reservation))")
    ReservationResponse toDto(Reservation reservation);

    default String getClientName(Reservation reservation) {
        if (reservation.getApprovedBy() == null) return null;
        User client = reservation.getApprovedBy();
        if (client.getFirstName() != null && client.getLastName() != null) {
            return client.getFirstName() + " " + client.getLastName();
        }
        return client.getUsername();
    }

    default String getClientFirstName(Reservation reservation) {
        if (reservation.getApprovedBy() == null) return null;
        return reservation.getApprovedBy().getFirstName();
    }

    default String getClientLastName(Reservation reservation) {
        if (reservation.getApprovedBy() == null) return null;
        return reservation.getApprovedBy().getLastName();
    }

    default String getClientPhoneNumber(Reservation reservation) {
        if (reservation.getApprovedBy() == null) return null;
        return reservation.getApprovedBy().getPhoneNumber();
    }

    default String getManagerPhoneNumber(Reservation reservation) {
        if (reservation.getUnit() == null || 
            reservation.getUnit().getAccommodation() == null ||
            reservation.getUnit().getAccommodation().getOwnerId() == null) {
            return null;
        }
        User manager = reservation.getUnit().getAccommodation().getOwnerId();
        return manager.getPhoneNumber();
    }
}

