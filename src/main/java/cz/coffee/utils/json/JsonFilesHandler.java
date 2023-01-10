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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.coffee.utils.SimpleUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static cz.coffee.utils.ErrorHandler.*;
import static cz.coffee.utils.ErrorHandler.Level.WARNING;

public class JsonFilesHandler {

    public static boolean canBeCreated(@NotNull File file) {
        return file.getParentFile().exists();
    }

    /**
     *
     * Create new json file with data, otherwise without as empty object
     *
     * @param inputString {@link String}
     * @param inputData {@link JsonElement}
     * @param forcing {@link Boolean}
     */
    public boolean newFile(@NotNull String inputString, JsonElement inputData, boolean forcing) {
        File file = new File(inputString);
        if (file.exists()) {
            if (file.length() < 0x01) sendMessage(JSON_FILE_EXISTS, WARNING); return false;
        }
        if (forcing) {
            Path fileParents = Paths.get(inputString);
            if (Files.exists(fileParents.getParent())) {
                sendMessage(PARENT_DIRECTORY_EXISTS, WARNING); return false;
            }
            try {
                Files.createDirectories(fileParents.getParent());
            } catch (IOException ioException) {
                sendMessage(PARENT_DIRECTORY_EXISTS, WARNING); return false;
            }
        }
        try (var ptw = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            if (inputData == null || inputData == JsonNull.INSTANCE)
                ptw.jsonValue("{}");
            else
                ptw.jsonValue(SimpleUtil.gsonAdapter.toJson(inputData));
            ptw.flush();
            ptw.close();
            return true;
        } catch (IOException ioException) {
            if (!canBeCreated(file)) sendMessage(PARENT_DIRECTORY_NOT_EXIST, WARNING);
            sendMessage(ioException.getMessage(), WARNING);
            return false;
        }
    }

    public JsonElement readFile(@NotNull String inputString) {
        JsonElement element = null;
        File file = new File(inputString);
        try (var ptr = new JsonReader(new FileReader(file))) {
            element = JsonParser.parseReader(ptr);
        } catch (IOException | JsonSyntaxException exception) {
            if (exception instanceof IOException) {
                if(!file.exists()) sendMessage(FILE_NOT_EXIST + inputString, WARNING);
            } else {
                sendMessage((exception).getMessage(), WARNING);
            }
        }
        return element;
    }
}
