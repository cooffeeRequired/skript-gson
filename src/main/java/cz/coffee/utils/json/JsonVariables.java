package cz.coffee.utils.json;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.utils.SimpleUtil;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JsonVariables {

    /**
     *
     * This function convert from JsonPrimitive to Object and then set it to Variable.
     *
     * @param name Variable name
     * @param element Any {@link JsonPrimitive}
     * @param event {@link Event}
     * @param isLocal {@link Boolean} if a variable is local or nah
     */
    public static void setPrimitiveType(String name, JsonPrimitive element, Event event, boolean isLocal) {
        if (element.isBoolean()) {
            setVariable(name, element.getAsBoolean(), event, isLocal);
        } else if (element.isNumber()) {
            setVariable(name, element.getAsDouble(), event, isLocal);
        } else if (element.isString()) {
            setVariable(name, element.getAsString(), event, isLocal);
        }
    }
    /**
     *
     * This function setting the value to variable
     *
     * @param name Variable name
     * @param element Any {@link JsonPrimitive}
     * @param event {@link Event}
     * @param isLocal {@link Boolean} if a variable is local or nah
     */
    public static void setVariable(String name, Object element, Event event, boolean isLocal) {
        Variables.setVariable(name, element, event, isLocal);
    }

    /**
     *
     * This function will get data from variable.
     *
     * @param name Variable name
     * @param isLocal {@link Boolean} if a variable is local or nah
     * @return {@link Object}
     */
    public static Object getVariable(Event e, String name, boolean isLocal) {
        final Object variable = Variables.getVariable(name, e, isLocal);
        if (variable == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return variable;
    }

    public static Object getSkriptVariable(Object input, Event e) {
        boolean isLocal = false;
        JsonElement newJsonElement;
        JsonObject output = new JsonObject();
        HashMap<String, Object> returnMap = new HashMap<>();
        String name = input.toString().replaceAll("[{}]", "");
        if (name.startsWith("$_")) {
            isLocal = true;
            name = name.replaceAll("_", "").replaceAll("[$]", "Variable.");
        }
        Object variable = Variables.getVariable(name.replaceAll("Variable.", ""), e, isLocal);

        newJsonElement = SimpleUtil.gsonAdapter.toJsonTree(variable);
        if (variable == null)
            newJsonElement = new JsonPrimitive(false);

        output.add("variable", newJsonElement);
        returnMap.put(name, output);

        return returnMap;
    }


    public static JsonElement parseVariable(String rawString, Event e) {
        Matcher m = Pattern.compile("\\$\\{.+?}").matcher(rawString);
        rawString = rawString.replaceAll("(?<!^)[_{}*](?!$)", "").replaceAll("[$]", "Variable.");

        for (Iterator<Object> it = m.results().map(MatchResult::group).map(k -> getSkriptVariable(k, e)).iterator(); it.hasNext(); ) {
            String Value;
            JsonObject object = SimpleUtil.gsonAdapter.toJsonTree(it.next()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> map : object.entrySet()) {
                JsonObject json = map.getValue().getAsJsonObject();

                if (json.get("variable").isJsonObject()) {
                    Stream<String> keys = json.get("variable").getAsJsonObject().keySet().stream().filter(Objects::nonNull);
                    if (json.get("variable").getAsJsonObject().keySet().stream().filter(Objects::nonNull).allMatch(SimpleUtil::isNumeric)) {
                        JsonArray array = new JsonArray();
                        keys.forEach(k -> array.getAsJsonArray().add(json.get("variable").getAsJsonObject().get(k)));
                        Value = array.toString();
                    } else {
                        Value = json.getAsJsonObject().get("variable").toString();
                    }
                } else {
                    Value = json.get("variable").toString();
                }
                rawString = rawString.replaceAll(map.getKey(), Value);
            }
        }
        return JsonParser.parseString(rawString);
    }
}
