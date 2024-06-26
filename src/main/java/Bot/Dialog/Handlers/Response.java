package Bot.Dialog.Handlers;

import Bot.Dialog.Data.UserData;
import com.pengrad.telegrambot.request.BaseRequest;

/**
 * Record describing StateHandler response on corresponding update.
 *
 * @param userData    external user data after request handling.
 * @param botResponse requests that must be executed by bot in order to answer to user.
 */
public record Response(UserData userData, BaseRequest... botResponse) {

}
