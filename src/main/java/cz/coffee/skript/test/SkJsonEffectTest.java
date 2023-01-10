package cz.coffee.skript.test;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.utils.ErrorHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static cz.coffee.utils.ErrorHandler.sendMessage;

public class SkJsonEffectTest extends SimpleExpression<Boolean> {

    static {
        Skript.registerExpression(SkJsonEffectTest.class, Boolean.class, ExpressionType.SIMPLE,
                "[skJson] version check");
    }

    @Override
    protected Boolean[] get(Event e) {
        JsonElement element = null;
        try {
            element = JsonParser.parseString("{'A':'n'}");
        } catch (Exception exception) {
            sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
        }
        if (element != null) {
            return new Boolean[]{true};
        }
        return new Boolean[]{false};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
