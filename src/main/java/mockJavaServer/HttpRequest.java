package mockJavaServer;

import APIWrapper.json.BookRoomRequest;
import APIWrapper.json.GetFreeRoomsRequest;
import APIWrapper.json.QueryBookingsRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class HttpRequest {
    // Constants
    private final static String DELIMITER = "\r\n\r\n";
    private final static String NEW_LINE = "\r\n";
    private final static String HEADER_DELIMITER = ":";

    // Useful addition
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Request body destruction

    private final String message;

    private final HttpMethod method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;

    // Possible classes from json-parsing
    protected GetFreeRoomsRequest getFreeRoomsRequest;
    protected BookRoomRequest bookRoomRequest;
    protected QueryBookingsRequest queryBookingsRequest;

    public HttpRequest(String message) {
        this.message = message;

        String[] parts = this.message.split(DELIMITER);

        String head = parts[0];
        String[] headers = head.split(NEW_LINE);

        String[] firstLine = headers[0].split(" ");
        method = HttpMethod.valueOf(firstLine[0]);
        url = firstLine[1];

        this.headers = Collections.unmodifiableMap(
                new HashMap<>() {
                    {
                    for (int i = 1; i < headers.length; i++) {
                        String[] headerParts = headers[i].split(HEADER_DELIMITER, 2);
                        put(headerParts[0].trim(), headerParts[1].trim());
                    }
                    }
                }
        );

        String bodyLength = this.headers.get("Content-Length");
        int length = bodyLength != null ? Integer.parseInt(bodyLength) : 0;
        body = parts.length > 1 ? parts[1].trim().substring(0, length) : "";

        parseClassFromGson();
    }

    private void parseClassFromGson() {
        String[] urlSplit = url.split("/");

        if (url.equals("/rooms/free")) {
            getFreeRoomsRequest = gson.fromJson(body, GetFreeRoomsRequest.class);
        } else if (urlSplit.length == 4) {
            bookRoomRequest = gson.fromJson(body, BookRoomRequest.class);
        } else if (url.equals("/bookings/query")) {
            queryBookingsRequest = gson.fromJson(body, QueryBookingsRequest.class);
        }
    }

    public String getMessage() {
        return message;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}