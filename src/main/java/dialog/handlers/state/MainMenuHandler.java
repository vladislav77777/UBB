package dialog.handlers.state;

import APIWrapper.json.Booking;
import APIWrapper.requests.Request;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import dialog.config.IText;
import dialog.handlers.Response;
import dialog.handlers.StateHandler;
import dialog.userData.BotState;
import dialog.userData.UserData;

import java.util.List;

public class MainMenuHandler extends StateHandler {
    private final Request outlook = new Request("http://localhost:3000");

    @Override
    public Response handle(Update incomingUpdate, UserData data) {
        var message = incomingUpdate.message();
        if (message == null) {
            return new Response(data);
        } else if (message.text().equals(data.getLang().myReservationsBtn())) {
            return handleBookings(data);
        } else if (message.text().equals(data.getLang().newBookingBtn())) {
            return handleNewBooking(data);
        } else {
            return new Response(data);
        }
    }

    private Response handleBookings(UserData data) {
        var usr = data.getUserId();
        var lang = data.getLang();

        var bookings = outlook.getBookingsByUser(data.getEmail());
        var userReservations = bookingsMessageText(bookings, lang);

        var transition = new SendMessage(
                usr,
                lang.goToBookings()
        ).replyMarkup(new ReplyKeyboardRemove());
        var bookingsMsg = new SendMessage(usr, userReservations).
                replyMarkup(lang.userBookings(bookings));

        data.setDialogState(BotState.LIST_OF_RESERVATIONS);
        return new Response(data, transition, bookingsMsg);
    }

    private Response handleNewBooking(UserData data) {
        var lang = data.getLang();
        var usr = data.getUserId();

        var botMessage = new SendMessage(
                usr, lang.chooseBookingTime())
                .replyMarkup(new ReplyKeyboardRemove());
        data.setDialogState(BotState.BOOKING_TIME_AWAITING);
        return new Response(data, botMessage);
    }

    private String bookingsMessageText(List<Booking> bookings, IText lang) {
        StringBuilder text;
        if (bookings == null || bookings.isEmpty()) {
            text = new StringBuilder(lang.noActualBookings());
        } else {
            text = new StringBuilder(lang.hereActualBookings());
        }
        return text.toString();
    }
}