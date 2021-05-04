package com.keyman.watcher.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TO_JSON_ERROR = "cannot convert to json";
    private static final String READ_JSON_ERROR = "cannot read json and get node";
    private static final String TO_OBJECT_ERROR = "cannot read json and convert node to object";
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getNodeAsString(String json, String fieldName){
        try {
            return OBJECT_MAPPER.readTree(json).get(fieldName).toString();
        } catch (JsonProcessingException e) {
            LOGGER.error(READ_JSON_ERROR, e);
        }
        return "";
    }

    public static String getNodeAsString(byte[] json, String fieldName){
        try {
            return OBJECT_MAPPER.readTree(json).get(fieldName).toString();
        } catch (IOException e) {
            LOGGER.error(READ_JSON_ERROR, e);
        }
        return "";
    }

    public static JsonNode getNode(String json, String fieldName){
        try {
            return OBJECT_MAPPER.readTree(json).get(fieldName);
        } catch (JsonProcessingException e) {
            LOGGER.error(READ_JSON_ERROR, e);
        }
        return null;
    }

    public static JsonNode getNode(byte[] json, String fieldName){
        try {
            return OBJECT_MAPPER.readTree(json).get(fieldName);
        } catch (IOException e) {
            LOGGER.error(READ_JSON_ERROR, e);
        }
        return null;
    }

    public static <T> T getNodeAsObject(String json, String fieldName, Class<T> clazz){
        try {
            String node = getNodeAsString(json, fieldName);
            return OBJECT_MAPPER.readValue(node, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error(TO_OBJECT_ERROR, e);
        }
        return null;
    }

    public static <T> T getNodeAsObject(byte[] json, String fieldName, Class<T> clazz){
        try {
            String node = getNodeAsString(json, fieldName);
            return OBJECT_MAPPER.readValue(node, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error(TO_OBJECT_ERROR, e);
        }
        return null;
    }

    public static <T, C extends Collection<T>> C getNodeAsCollection(String json, String fieldName, TypeReference<C> typeReference){
        try {
            String node = getNodeAsString(json, fieldName);
            return OBJECT_MAPPER.readValue(node, typeReference);
        } catch (JsonProcessingException e) {
            LOGGER.error(TO_OBJECT_ERROR, e);
        }
        return null;
    }

    public static <T, C extends Collection<T>> C getNodeAsCollection(byte[] json, String fieldName, TypeReference<C> typeReference){
        try {
            String node = getNodeAsString(json, fieldName);
            return OBJECT_MAPPER.readValue(node, typeReference);
        } catch (JsonProcessingException e) {
            LOGGER.error(TO_OBJECT_ERROR, e);
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> clazz){
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error(TO_JSON_ERROR, e);
        }
        return null;
    }

    public static <T> T fromJson(byte[] json, Class<T> clazz){
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            LOGGER.error(TO_JSON_ERROR, e);
        }
        return null;
    }

    public static <C> C fromJson(byte[] json, TypeReference<C> typeReference){
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            LOGGER.error(TO_JSON_ERROR, e);
        }
        return null;
    }

    public static boolean isJsonFormat(String json) {
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
//            LOGGER.warn(String.format("not a valid json:  %s", json), e);
        }
        return false;
    }

    public static <C> C fromJson(String json, TypeReference<C> typeReference){
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            LOGGER.error(TO_JSON_ERROR, e);
        }
        return null;
    }

    public static <T> byte[] writeToByte(T t){
        try {
            return OBJECT_MAPPER.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            LOGGER.error("cannot convert object to byte array", e);
        }
        return null;
    }

    public static <T> String writeToString(T t){
        try {
            return OBJECT_MAPPER.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            LOGGER.error("cannot convert object to string", e);
        }
        return null;
    }
}
