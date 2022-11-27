package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EffTest extends Effect {

    private Expression<String> code;
    private Expression<Objects> data;
    private VariableString var;

    static {
        Skript.registerEffect(EffTest.class, "test skript-gson %string% %-objects%");
    }

    @Override
    protected void execute(@NotNull Event e) {

        String code2 = code.getSingle(e);
        Object[] data2 = data.getAll(e);


        JsonElement json = JsonParser.parseString("{'A': {'B': {}}}");
        JsonElement parse = JsonParser.parseString("{'A': 'XXXXXXXXXXX'}");

        if (Objects.equals(code2, "new utils")) {
            System.out.println("APPEND " + GsonUtils.append(json, parse, null, "A:B"));

            System.out.println("CHECK KEY " + GsonUtils.check(json, "B", GsonUtils.Type.KEY));
            System.out.println("CHECK VALUE NON JSON " + GsonUtils.check(json, "1", GsonUtils.Type.VALUE));
            System.out.println("CHECK VALUE JSON " + GsonUtils.check(json, "{'B': 1}", GsonUtils.Type.VALUE));


            System.out.println("Change KEY " + GsonUtils.change(json, "A", "Y", GsonUtils.Type.KEY));
            System.out.println("Change VALUE " + GsonUtils.change(json, "B", parse, GsonUtils.Type.VALUE));
        } else if (Objects.equals(code2, "listToJson")) {
            if (var != null)
                System.out.println(GsonUtils.GsonMapping.listToJson(e, data2[0].toString().substring(0, data2[0].toString().length() - 1), true));
            else
                Skript.debug("Do you forgot the name of variable?");
        } else if (Objects.equals(code2, "jsonToList")) {
            if (var != null) {
                String clearVarName;
                clearVarName = var.toString(e).substring(0, var.toString(e).length() - 3);
                GsonUtils.GsonMapping.jsonToList(e, clearVarName, json, true);
                clearVarName = var.toString().substring(0, var.toString().length() - 1).replaceFirst("\"", "").replaceAll("[*]", "");

                JsonElement element = GsonUtils.GsonMapping.listToJson(e, clearVarName, true);

                if(element.toString().equalsIgnoreCase(json.toString()))
                    SkriptGson.info("test &e\"jsonToList\" &aSuccessfully");
                else
                    SkriptGson.info("test &e\"jsonToList\" &cFailed");
            } else {
                Skript.debug("Do you forgot the name of variable?");
            }

        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        code = (Expression<String>) exprs[0];
        data = (Expression<Objects>) exprs[1];

        if (data instanceof Variable<?> variable) {
            if (variable.isList()) {
                var = variable.getName();
                return true;
            }
        }

        return true;
    }
}
