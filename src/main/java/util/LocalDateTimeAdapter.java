package util; // Or any package you prefer for utility classes

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    // Use ISO_LOCAL_DATE_TIME for "yyyy-MM-ddTHH:mm:ss" format, which is common for LocalDateTime.
    // Adjust if your JSON date-time format is different (e.g., includes milliseconds: DateTimeFormatter.ISO_DATE_TIME)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(FORMATTER));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        } catch (java.time.format.DateTimeParseException e) {
            throw new JsonParseException("Failed to parse LocalDateTime: " + json.getAsString(), e);
        }
    }
}