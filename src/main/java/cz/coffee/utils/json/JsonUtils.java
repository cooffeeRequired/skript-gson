/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils.json;

import com.google.gson.*;
import cz.coffee.utils.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;

import static cz.coffee.utils.ErrorHandler.Level.WARNING;
import static cz.coffee.utils.ErrorHandler.NESTED_KEY_MISSING;
import static cz.coffee.utils.ErrorHandler.sendMessage;
import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.SimpleUtil.isNumeric;
import static cz.coffee.utils.Type.KEY;
import static cz.coffee.utils.Type.VALUE;


/**
 * Class {@link JsonUtils}
 * Since 2.0
 */

@SuppressWarnings("unused")
public class JsonUtils {
    /**
     *
     * @param input any {@link JsonElement}
     * @param searchedTerm The expression we are looking for in the json object.
     * @param type The type we are looking for, either the key or the value
     * @return {@link Boolean}
     */
    public static boolean check (@NotNull JsonElement input, String searchedTerm, @NotNull Type type) {
        JsonElement element;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(input);

        while ((element = elements.pollFirst()) != null ) {
            if (element.isJsonArray()) {
                JsonArray elementArray = element.getAsJsonArray();
                for (JsonElement term : elementArray) {
                    if (Objects.equals(term.toString(), searchedTerm)) return true;
                    elements.offerLast(term);
                }
            } else if (element.isJsonObject()) {
                JsonObject elementObject = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : elementObject.entrySet()) {
                    if (type == KEY) {
                        if (entry.getKey().equals(searchedTerm)) return true;
                        if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                    } else if (type == Type.VALUE) {
                        JsonElement parsedData = JsonParser.parseString(searchedTerm);
                        if (entry.getValue().equals(parsedData)) return true;
                        elements.offerLast(entry.getValue());
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param input any {@link JsonElement}
     * @param from searched expression for its fundamental value.
     * @param to the final data that will be changed for the given key.
     * @return will return the changed {@link JsonElement}
     */
    public JsonElement changeJson(@NotNull JsonElement input, @NotNull String from, Object to) {
        JsonElement element;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(input);

        // prepare variable for nested keys.
        boolean isNested = false;
        String[] fromAsList = new String[0];

        if (from.contains(":")) {
            isNested = true;
            fromAsList = from.split(":");
        }

        while ((element = elements.pollFirst()) != null) {
            if (element instanceof JsonObject) {
                JsonObject elementObject = element.getAsJsonObject();
                for(Map.Entry<String, JsonElement> entry : elementObject.entrySet()) {
                    if(!entry.getKey().equals(isNested ? fromAsList[fromAsList.length -1] : from)) {
                        elements.offerLast(entry.getValue());
                    } else {
                        if (entry.getValue() instanceof JsonPrimitive) {
                            if (to instanceof Number) elementObject.addProperty(entry.getKey(), ((Number) to));
                            else if (to instanceof String) elementObject.addProperty(entry.getKey(), ((String) to));
                            else if (to instanceof Boolean) elementObject.addProperty(entry.getKey(), ((Boolean) to));
                            else
                                elementObject.add(entry.getKey(), gsonAdapter.toJsonTree(to));
                        } else {
                            elementObject.add(entry.getKey(), gsonAdapter.toJsonTree(to));
                        }
                    }
                }
            } else if (element instanceof JsonArray) {
                for (JsonElement data : element.getAsJsonArray()) {
                    elements.offerLast(data);
                }
            }
        }
        return input;
    }

    /**
     *
     * @param fromSourceInput {@link NotNull} {@link JsonElement} loaded directly from json file / variable / json map
     * @param inputToAppend {@link NotNull} {@link JsonElement} customer input's json
     * @param key searched expression from value:key pair..
     * @param nested expression what contains a nested path
     * @return changed {@link JsonElement}
     */
    public JsonElement appendJson(@NotNull JsonElement fromSourceInput, @NotNull JsonElement inputToAppend, String key, String nested) {
        String[] nests = (nested.contains(":")) ? nested.split(":") : new String[]{nested};
        boolean isArrayKey = false;
        for (String elementOfNestedPath : nests) {
            if (isNumeric(elementOfNestedPath)) isArrayKey = true;
            if (!check(fromSourceInput, elementOfNestedPath, KEY)) {
                sendMessage(NESTED_KEY_MISSING, WARNING);return null;
            }
            if (fromSourceInput instanceof JsonArray) fromSourceInput = fromSourceInput.getAsJsonArray().get(isArrayKey ? Integer.parseInt(elementOfNestedPath) : 0);
            else if (fromSourceInput instanceof JsonObject) fromSourceInput = fromSourceInput.getAsJsonObject().get(elementOfNestedPath);
        }
        if (fromSourceInput instanceof JsonObject) fromSourceInput.getAsJsonObject().add(key == null ? String.valueOf(fromSourceInput.getAsJsonObject().size()) : key, inputToAppend);
        else if (fromSourceInput instanceof JsonArray) fromSourceInput.getAsJsonArray().add(inputToAppend);
        return fromSourceInput;
    }

    /**
     *
     * @param search equivalent parameter of the search term
     * @param fromSourceInput {@link JsonElement} input
     * @param type {@link Type}
     * @return count of {@link Integer}
     */
    public int count(@NotNull String search, @NotNull JsonElement fromSourceInput, Type type) {
        int count = 0;
        JsonElement value;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(fromSourceInput);

        while ((value = elements.pollFirst()) != null) {
            if (value instanceof JsonArray) {
                for (JsonElement l : value.getAsJsonArray()) elements.offerLast(l);
            } else if (value instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                    if (type == KEY) {
                        if (entry.getKey().equals(search)) count++;
                        if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                    } else if (type == VALUE) {
                        JsonElement parsedValue = JsonParser.parseString(search);
                        if (entry.getValue().equals(parsedValue)) count++; elements.offerLast(entry.getValue());
                    }
                }
            }
        }
        return count;
    }

    /**
     *
     * @param Object {@link String} Any expression on which .toString() can be performed
     * @return {@link JsonPrimitive}
     */
    public static JsonElement fromString2JsonElement(Object Object) {
        if (Object instanceof Integer) {
            return new JsonPrimitive((Integer) Object);
        } else if (Object instanceof String) {
            try {
                return JsonParser.parseString((String) Object);
            } catch (JsonSyntaxException exception) {
                return JsonParser.parseString(gsonAdapter.toJson(Object));
            }
        } else if (Object instanceof Boolean)
            return new JsonPrimitive(((Boolean) Object));

        return null;
    }

    /**
     *
     * @param primitive any {@link JsonPrimitive} value
     * @return Object.
     */
    public static Object fromPrimitive2Object(JsonPrimitive primitive) {
        if (primitive.isBoolean()) return primitive.getAsBoolean();
        if (primitive.isNumber()) return primitive.getAsNumber();
        if (primitive.isString()) return primitive.getAsString();
        return null;
    }
}
