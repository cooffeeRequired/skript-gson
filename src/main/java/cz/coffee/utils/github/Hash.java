package cz.coffee.utils.github;

import cz.coffee.utils.ErrorHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static cz.coffee.utils.ErrorHandler.sendMessage;

public class Hash {
    private String hash;


    public Hash(URL input, String alg) {
        try {
            URLConnection urlConnection = input.openConnection();
            hash = process(urlConnection.getInputStream(), alg);
        } catch (Exception exception) {
            sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
        }
    }

    public Hash(File input, String alg) {
        try {
            hash = process(new FileInputStream(input), alg);
        } catch (IOException ioException) {
            sendMessage(ioException.getMessage(), ErrorHandler.Level.ERROR);
        }
    }

    private String process(InputStream input, String alg){
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance(alg);
            try (InputStream fis = input) {
                byte[] dataBytes = new byte[1024];
                int read;
                while ((read = fis.read(dataBytes)) != -1)
                    md5.update(dataBytes, 0, read);
                byte[] mdBytes = md5.digest();
                for (byte mdByte : mdBytes) sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException | IOException exception) {
            sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
        }
        int MAX_LENGTH = 64;
        if (sb.toString().length() == MAX_LENGTH) {
            return sb.toString();
        } else {
            return null;
        }
    }



    public String get(){
        return hash;
    }
}
