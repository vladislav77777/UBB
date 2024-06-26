package APIWrapper.Requests;

import Models.*;
import Utilities.Services;

import java.util.ArrayList;

public class Request {
    private final RequestFormatted formatter;

    public Request() {
        String url = Services.getEnv("MOCK_SERVER_URL");
        formatter = new RequestFormatted(url);
    }


    public ArrayList<Room> getAllBookableRooms() {
        return formatter.getAllBookableRooms();
    }

    public ArrayList<Room> getAllFreeRooms(GetFreeRoomsRequest request) {
        return formatter.getAllFreeRooms(request);
    }

    public Booking bookRoom(String roomId, BookRoomRequest request) {
        return formatter.bookRoom(roomId, request);
    }

    public ArrayList<Booking> queryBookings(QueryBookingsRequest request) {
        return formatter.queryBookings(request);
    }

    public String deleteBooking(String bookingId) {
        return formatter.deleteBooking(bookingId);
    }

    public ArrayList<Booking> getBookingsByUser(String email) {
        BookingsFilter bookingsFilter = new BookingsFilter(
                null,
                null,
                new String[]{},
                new String[]{email}
        );

        return queryBookings(new QueryBookingsRequest(bookingsFilter));
    }

    public Booking getBookingById(String bookingId) {
        BookingsFilter bookingsFilter = new BookingsFilter(
                null,
                null,
                new String[]{},
                new String[]{}
        );

        return queryBookings(new QueryBookingsRequest(bookingsFilter)).stream().filter(
                booking -> booking.id.equals(bookingId)
        ).findFirst().orElse(null);
    }
}
