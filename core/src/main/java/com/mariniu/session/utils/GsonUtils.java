package com.mariniu.session.utils;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created on 06/10/16.
 *
 * @author Umberto Marini
 */
public class GsonUtils {

    /**
     * Creates a {@link Gson} instance.
     *
     * @return an instance of Gson configured with the options currently set in this builder
     */
    public static Gson create() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

    public static Gson createSerializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Uri.class, new UriSerializer());
        return builder.create();
    }

    public static Gson createDeserializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        return builder.create();
    }

    public static class UriSerializer implements JsonSerializer<Uri> {

        @Override
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static class UriDeserializer implements JsonDeserializer<Uri> {

        @Override
        public Uri deserialize(final JsonElement src, final Type srcType, final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }
}
