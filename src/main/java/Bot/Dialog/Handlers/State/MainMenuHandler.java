package Bot.Dialog.Handlers.State;

import APIWrapper.Requests.Request;
import Bot.Dialog.Config.EnglishText;
import Bot.Dialog.Config.RussianText;
import Bot.Dialog.Data.BotState;
import Bot.Dialog.Data.UserData;
import Bot.Dialog.Handlers.Response;
import Bot.Dialog.Handlers.StateHandler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;

public class MainMenuHandler extends StateHandler {
    private final Request outlook = new Request();

    @Override
    public Response handle(Update incomingUpdate, UserData data) {
        var message = incomingUpdate.message();
        if (message == null) {
            return new Response(data);
        } else if (message.text().equals(data.getLang().myReservationsBtn())) {
            return handleBookings(data);
        } else if (message.text().equals(data.getLang().newBookingBtn())) {
            return handleNewBooking(data);
        } else if (message.text().equals(data.getLang().changeLanguage())) {
            return changeLanguage(data);
        } else {
            return new Response(data);
        }
    }

    /**
     * Handle change language request
     *
     * @param data user data
     * @return bot response
     */
    private Response changeLanguage(UserData data) {
        var usr = data.getUserId();
        var lang = data.getLang();
        var newLang = (lang instanceof EnglishText ? new RussianText() : new EnglishText());
        data.setLang(newLang);

        var msg = new SendMessage(usr, newLang.languageChanged()).replyMarkup(newLang.mainMenuMarkup());
        return new Response(data, msg);
    }

    /**
     * Handle transition to list of bookings
     *
     * @param data user data
     * @return bot response
     */
    private Response handleBookings(UserData data) {
        var usr = data.getUserId();
        var lang = data.getLang();

        var bookings = outlook.getBookingsByUser(data.getEmail());
        var actualBookings = lang.actualBookings(bookings);

        var transition = new SendMessage(
                usr,
                lang.goToBookings()
        ).replyMarkup(new ReplyKeyboardRemove());
        var bookingsMsg = new SendMessage(usr, actualBookings).
                replyMarkup(lang.userBookings(bookings));

        data.setDialogState(BotState.LIST_OF_RESERVATIONS);
        return new Response(data, transition, bookingsMsg);
    }

    /**
     * Handle initial stage of booking creation.
     *
     * @param data user data
     * @return bot response
     */
    private Response handleNewBooking(UserData data) {
        var lang = data.getLang();
        var usr = data.getUserId();

        var botMessage = new SendMessage(
                usr, lang.chooseBookingTime())
                .replyMarkup(new ReplyKeyboardRemove());

        data.setDialogState(BotState.BOOKING_TIME_AWAITING);
        return new Response(data, botMessage);
    }
}