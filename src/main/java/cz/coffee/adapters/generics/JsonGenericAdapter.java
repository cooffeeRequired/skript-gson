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
package cz.coffee.adapters.generics;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;


/**
 * <p>
 * The class represent a serializer/deserializer for another object than @ConfigurationSerializable
 * the {@link JsonGenericAdapter} interface javadocs.
 */
@SuppressWarnings("unused")

public interface JsonGenericAdapter<T> {

    String GSON_GENERIC_ADAPTER_KEY = "??";

    /**
     * <p>
     * This method will return a deserialization Object {@link T}
     *
     * @return JsonElement
     * </p>
     */
    @NotNull JsonElement toJson(T object);

    /**
     * <p>
     * This method will return a serialization {@link JsonElement} from {@link T}
     *
     * @return T
     * </p>
     */
    T fromJson(JsonElement json);


    /**
     * <p>
     * This method will check what type of serialized Json contain. {@link JsonElement}
     *
     * @return Clazz
     * </p>
     */
    Class<? extends T> typeOf(JsonElement json);

}