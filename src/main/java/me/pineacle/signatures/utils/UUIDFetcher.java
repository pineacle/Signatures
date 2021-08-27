package me.pineacle.signatures.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UUIDFetcher {

    private ExecutorService executor;

    public UUIDFetcher(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public UUID fetchUUID(String playerName) throws Exception {
        // Get response from Mojang API
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if(connection.getResponseCode() == 400) {
            System.err.println("There is no player with the name \"" + playerName + "\"!");
            return UUID.randomUUID();
        }

        InputStream inputStream = connection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // Parse JSON response and get UUID
        JsonElement element = new JsonParser().parse(bufferedReader);
        JsonObject object = element.getAsJsonObject();
        String uuidAsString = object.get("id").getAsString();

        // Return UUID
        return parseUUIDFromString(uuidAsString);
    }


    public void fetchUUIDAsync(String playerName, Consumer<UUID> consumer) {
        executor.execute(() -> {

            try {
                // Get response from Mojang API
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if(connection.getResponseCode() == 400) {
                    System.err.println("There is no player with the name \"" + playerName + "\"!");
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // Parse JSON response and get UUID
                JsonElement element = new JsonParser().parse(bufferedReader);
                JsonObject object = element.getAsJsonObject();
                String uuidAsString = object.get("id").getAsString();

                inputStream.close();
                bufferedReader.close();

                // Return UUID
                consumer.accept(parseUUIDFromString(uuidAsString));
            } catch (IOException e) {
                System.err.println("Couldn't connect to URL.");
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }

    private UUID parseUUIDFromString(String uuidAsString) {
        String[] parts = {
                "0x" + uuidAsString.substring(0, 8),
                "0x" + uuidAsString.substring(8, 12),
                "0x" + uuidAsString.substring(12, 16),
                "0x" + uuidAsString.substring(16, 20),
                "0x" + uuidAsString.substring(20, 32)
        };

        long mostSigBits = Long.decode(parts[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parts[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parts[2]).longValue();

        long leastSigBits = Long.decode(parts[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(parts[4]).longValue();

        return new UUID(mostSigBits, leastSigBits);
    }

}