package cz.coffee.skriptgson.skript.effect;


import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.adapters.Adapters;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.FILE_JSON_HASHMAP;
import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.WARNING;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.*;
import static cz.coffee.skriptgson.utils.GsonUtils.GsonFileHandler.saveToFile;
import static cz.coffee.skriptgson.utils.GsonUtils.setVariable;


@Name("Write jsonelement/cached Json/Json file")
@Description({"You can write/re-write the jsonelement or the cached json or the json file"})
@Examples(value = {"command sk-example:",
        "\ttrigger:",
        "\t\twrite player's location to cached json \"your\"",
        "\t\tsend cached json \"your\" with pretty print",
        "",
        "\t\tset {_json} to new json from player's world",
        "\t\twrite player's location to {_json}",
        "\t\tsend {_json} with pretty print",
        "",
        "\t\tset {_fileJson} to new json from file path \"sk-gson\\test.json\"",
        "\t\twrite player's location to file \"sk-gson\\test.json\"",
        "\t\tset {_fileJson} to new json from file path \"sk-gson\\test.json\"",
        "\t\tsend {_fileJson} with pretty print",
})
@Since("2.0.0")

public class EffWriteJsonElement extends Effect {
    static {
        Skript.registerEffect(EffWriteJsonElement.class,
                "write [data] %object% to (1:%-jsonelement%|2:file [path] %-string%|3:[cached] json[(-| )id] %-string%)",
                "write item %itemstack% to (1:%-jsonelement%|2:file [path] %-string%|3:[cached] json[(-| )id] %-string%)"
        );
    }

    private boolean isJson, isCached, isFile, isItem;
    private boolean isLocal;
    private int pattern;
    private VariableString variableString;
    private Expression<Object> dataExpression;
    private Expression<Object> fromGenericExpression;
    private Expression<ItemType> fromItemType;

    @Override
    protected void execute(@NotNull Event e) {
        Object fromGeneric;

        if (!isItem) {
            fromGeneric = this.fromGenericExpression.getSingle(e);
        } else {
            fromGeneric = this.fromItemType.getSingle(e);
        }
        if (fromGeneric == null) return;

        if (isJson) {
            String variableName = variableString.getDefaultVariableName().replaceAll("_", "");
            Object isJsonVar = dataExpression.getSingle(e);
            if (!(isJsonVar instanceof JsonElement)) {
                sendErrorMessage(ONLY_JSONVAR_IS_ALLOWED, WARNING);
                return;
            }
            setVariable(variableName, Adapters.toJson(fromGeneric), e, isLocal);
        } else if (isFile) {
            Object objectFilePath = dataExpression.getSingle(e);
            if (objectFilePath == null) return;
            String filepathString = objectFilePath.toString();
            saveToFile(Adapters.toJson(fromGeneric), filepathString);
        } else if (isCached) {
            Object objectFilePath = dataExpression.getSingle(e);
            if (objectFilePath == null) return;

            if (FILE_JSON_HASHMAP.containsKey(objectFilePath.toString())) {
                if (JSON_HASHMAP.containsKey(objectFilePath.toString())) {
                    JSON_HASHMAP.remove(objectFilePath.toString());
                    JSON_HASHMAP.put(objectFilePath.toString(), fromGeneric);
                }
            }
        }
    }


    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        isItem = (matchedPattern == 1);
        if (isItem) {
            fromItemType = LiteralUtils.defendExpression(exprs[0]);
        } else {
            fromGenericExpression = LiteralUtils.defendExpression(exprs[0]);
        }

        // parser marks
        isJson = (parseResult.mark == 1);
        isFile = (parseResult.mark == 2);
        isCached = (parseResult.mark == 3);

        if (isJson) {
            dataExpression = (Expression<Object>) exprs[1];
            if (dataExpression instanceof Variable<?> variable) {
                if (variable.isSingle()) {
                    isLocal = variable.isLocal();
                    variableString = variable.getName();
                } else {
                    sendErrorMessage(VAR_NEED_TO_BE_SINGLE, WARNING);
                    return false;
                }
            } else {
                sendErrorMessage(ONLY_JSONVAR_IS_ALLOWED, WARNING);
                return false;
            }
        } else if (isFile) {
            dataExpression = (Expression<Object>) exprs[2];
        } else if (isCached) {
            dataExpression = (Expression<Object>) exprs[3];
        }
        if (isItem) {
            return LiteralUtils.canInitSafely(fromItemType);
        } else {
            return LiteralUtils.canInitSafely(fromGenericExpression);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "write " + (pattern == 0 ? "data " + fromGenericExpression.toString() : "item " + fromItemType.toString()) + "to " + (isJson ? dataExpression.toString() : null) + (isFile ? "file path " + dataExpression.toString() : null) + (isCached ? "cached json " + dataExpression.toString() : null);
    }
}
