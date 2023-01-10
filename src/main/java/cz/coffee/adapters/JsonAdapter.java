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
package cz.coffee.adapters;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.adapters.generics.JsonGeneric;
import cz.coffee.adapters.generics.JsonInventory;
import cz.coffee.adapters.generics.JsonItemStack;
import cz.coffee.adapters.generics.JsonWorld;
import cz.coffee.utils.SimpleUtil;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class JsonAdapter {
    public static JsonElement toJson(Object input) {
        if (input != null) {
            boolean isSerializable = (input instanceof YggdrasilSerializable || input instanceof ConfigurationSerializable);
            if (isSerializable)
                return SimpleUtil.gsonAdapter.toJsonTree(input);
            else {
                if (input instanceof World) {
                    World world =  (World) input;
                    return new JsonWorld().toJson(world);
                } else if (input instanceof Inventory) {
                    Inventory inventory = (Inventory) input;
                    return new JsonInventory().toJson(inventory);
                }
            }
        }
        return null;
    }
    public static Object fromJson(JsonElement json) {
        if (json instanceof JsonNull) return null;
        Class<?> clazz = new JsonGeneric().typeOf(json);

        if (Inventory.class.isAssignableFrom(clazz)) {
            return new JsonInventory().fromJson(json);
        } else if (World.class.isAssignableFrom(clazz)) {
            return new JsonWorld().fromJson(json);
        } else {
            Object returnData = SimpleUtil.gsonAdapter.fromJson(json, ConfigurationSerializable.class);
            if (returnData instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) returnData;
                JsonItemStack jsonItem = new JsonItemStack(itemStack);
                jsonItem.setOthers(json);
                return jsonItem.getItemStack();
            }
            return returnData;
        }
    }
}
