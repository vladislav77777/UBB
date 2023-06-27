package dialog.handlers.state;

import APIWrapper.json.Booking;
import APIWrapper.json.GetFreeRoomsRequest;
import APIWrapper.json.Room;
import APIWrapper.requests.Request;
import APIWrapper.utilities.DateTime;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import dialog.handlers.Response;
import dialog.handlers.StateHandler;
import dialog.userData.BotState;
import dialog.userData.UserData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * States Group handler
 */
// TODO: implement a class to work with user booking parameters at parse it to request
public class NewBookingHandler extends StateHandler {
    private final Map<Long, Booking> bookingInfo = new HashMap<>();
    private final Request outlook = new Request("http://localhost:3000");
    private final List<Room> rooms =
            outlook.getAllBookableRooms();
    @Override
    public Response handle(Update incomingUpdate, UserData data) {
        switch (data.getDialogState()) {
            case BOOKING_TIME_AWAITING -> {
                return handleBookingTime(incomingUpdate, data);
            }
            case BOOKING_DURATION_AWAITING -> {
                return handleBookingDuration(incomingUpdate, data);
            }
            case ROOM_AWAITING -> {
                return handleRoom(incomingUpdate, data);
            }
            case BOOKING_TITLE_AWAITING ->  {
                return handleTitle(incomingUpdate, data);
            }
            default -> {
                return new Response(data);
            }
        }
    }

    private Response handleTitle(Update update, UserData data) {
        var message = update.message();
        if (message == null) {
            return new Response(data);
        }

        var user = data.getUserId();
        var lang = data.getLang();
        var info = bookingInfo.get(user);

        info.title = message.text();
        var response = outlook.bookRoom(info.room.id,
                info.convertToBookRoomRequest());

        // TODO: Format via response from server
        var botMessage = new SendMessage(user,
                        lang.bookedSuccessfully(info.title,
                                info.room.name,
                                DateTime.formatToConvenient(info.start),
                                DateTime.formatToConvenient(info.end))).
                replyMarkup(lang.mainMenuMarkup());
        data.setDialogState(BotState.MAIN_MENU);
        return new Response(data, botMessage);
    }

    // Handle somehow

    private Response handleRoom(Update update, UserData data) {
        var query = update.callbackQuery();
        if (query == null) {
            return new Response(data);
        }

        var user = data.getUserId();
        var lang = data.getLang();
        var roomId = query.data();
        var chatId = query.message().chat().id();
        var msgId = query.message().messageId();
        var info = bookingInfo.get(user);
        // TODO: more precise handling of incorrect callbacks
        try {
            info.room = takeRoomById(roomId);
            assert info.room != null;
            var updateMessage = new EditMessageText(chatId, msgId, lang.chosenRoom(info.room.name));
            var botMessage = new SendMessage(
                    user,
                    lang.bookingTitle());
            data.setDialogState(BotState.BOOKING_TITLE_AWAITING);
            return new Response(data, botMessage, updateMessage);
        } catch (Exception e) {
            return new Response(data);
        }
    }

    private Response handleBookingDuration(Update update, UserData data) {
        var query = update.callbackQuery();
        if (query == null) {
            return new Response(data);
        }

        var user = data.getUserId();
        var chatId = query.message().chat().id();
        var msgId = query.message().messageId();
        var lang = data.getLang();
        var info = bookingInfo.get(user);
        // TODO: handle wrong callbacks more precisely
        try {
            info.duration = Integer.parseInt(query.data());
        } catch (Exception e) {
            return new Response(data);
        }


        var updateMessage =
                new EditMessageText(
                        chatId,
                        msgId,
                        lang.chosenBookingTime(
                                info.start,
                                String.valueOf(info.duration))
                );

        // TODO: properly obtain list of available rooms at given time (how to get time?)

        var userRooms = outlook.getAllFreeRooms(
                new GetFreeRoomsRequest("25.05.04 09:26", 90));

        if (userRooms.isEmpty()) {
            data.setDialogState(BotState.MAIN_MENU);
            return new Response(data, new SendMessage(user, data.getLang().noAvailableRooms()).
                    replyMarkup(lang.mainMenuMarkup()), updateMessage);
        } else {
            // TODO: check here for NOW AVAILABLE rooms
            var keyboardWithRooms = lang.availableRoomsKeyboard(userRooms);
            data.setDialogState(BotState.ROOM_AWAITING);
            return new Response(data, new SendMessage(user, data.getLang().hereAvailableRooms()).
                    replyMarkup(keyboardWithRooms), updateMessage);
        }
    }

    private Response handleBookingTime(Update update, UserData data) {
        if (update.message() == null) {
            return new Response(data);
        }

        var msg = update.message();
        var usr = data.getUserId();
        var lang = data.getLang();

        var maybeDate = msg.text().strip();

        if (!DateTime.isValid(maybeDate)) {
            return new Response(data, new SendMessage(usr, lang.invalidBookingTime()));
        }

        bookingInfo.put(usr, new Booking());
        bookingInfo.get(usr).owner_email = data.getEmail();
        bookingInfo.get(usr).start = msg.text();

        var botMsg =
                new SendMessage(usr, lang.
                        chooseBookingDuration()).
                        replyMarkup(lang.bookingDurations());

        data.setDialogState(BotState.BOOKING_DURATION_AWAITING);
        return new Response(data, botMsg);
    }

    // Utils methods

    /**
     * Find room instance by its id
     * @param roomId given id
     * @return room (it is supposed that given id always correct)
     */
    private Room takeRoomById(String roomId) {
        for (Room room : rooms) {
            if (room.id.equals(roomId)) {
                return room;
            }
        }
        return null;
    }
}
