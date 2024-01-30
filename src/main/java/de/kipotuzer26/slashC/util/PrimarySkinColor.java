package de.kipotuzer26.slashC.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.w3c.dom.Text;
import org.yaml.snakeyaml.introspector.Property;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getLogger;

public class PrimarySkinColor {
    public static Map<UUID, Color> playerColor = new HashMap();

    private static String decodeBase64(String base64File) {
        byte[] byteArray = Base64.getDecoder().decode(base64File);
        String decodedStr = new String(byteArray, StandardCharsets.UTF_8);
        return decodedStr;
    }


    public static Color getPrimaryColor(UUID player) {

        if(playerColor.containsKey(player)){
//            System.out.println("used Buffer");
            return playerColor.get(player);
        }

        URL url_1 = null;
        try {
            url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + player.toString() + "");

        InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
        JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        String texture = textureProperty.get("value").getAsString();
        String TextureURL = new JsonParser().parse(decodeBase64(texture)).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
        Color color = getAccentColor(TextureURL);
        playerColor.put(player, color);
        return color;
        } catch (IOException e) {
            e.printStackTrace();
            return new Color(0,0,0);
        }
    }

    public static Color loadColor(UUID player) {

        URL url_1 = null;
        try {
            url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + player.toString() + "");

            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String TextureURL = new JsonParser().parse(decodeBase64(texture)).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
            Color color = getAccentColor(TextureURL);
            playerColor.replace(player, color);
            return color;
        } catch (IOException e) {
            e.printStackTrace();
            return new Color(0,0,0);
        }
    }

    private static Color getInternalPrimaryColor(String textureUrl) {
        try {
            // Fetch the image from the URL
            BufferedImage skinImage = ImageIO.read(new URL(textureUrl));

            if (skinImage != null) {
                // Process the image to extract prominent colors
                return extractDominantColor(skinImage);
            } else {
                System.err.println("Error: Failed to fetch skin image from URL.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Handle the case where fetching or processing fails
    }


    private static Color getAccentColor(String textureUrl) {
        try {
            // Fetch the image from the URL
            BufferedImage skinImage = javax.imageio.ImageIO.read(new URL(textureUrl));

            if (skinImage != null) {
                // Count occurrences of each non-transparent color in the image
                Map<Integer, Integer> colorCount = new HashMap<>();

                for (int y = 0; y < skinImage.getHeight(); y++) {
                    for (int x = 0; x < skinImage.getWidth(); x++) {
                        int rgb = skinImage.getRGB(x, y);
                        int a = new Color(rgb).getAlpha();
                        // Skip transparent pixels
                        if ((rgb >> 24) == 0x00) {
                            continue;
                        }

                        colorCount.put(rgb, colorCount.getOrDefault(rgb, 0) + 1);
                    }
                }

                // Find the color with the highest count
                int maxCount = 0;
                int accentColorRGB = 0;

                for (Map.Entry<Integer, Integer> entry : colorCount.entrySet()) {
                    int count = entry.getValue();
                    if (count > maxCount) {
                        maxCount = count;
                        accentColorRGB = entry.getKey();
                    }
                }

                return new Color(accentColorRGB);
            } else {
                System.err.println("Error: Failed to fetch skin image from URL.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Handle the case where fetching or processing fails
    }

    private static Color extractDominantColor(BufferedImage image) {
        // Use BufferedImage to analyze the image and extract dominant color
        // This is a simplified example, and you might need to adjust parameters and methods based on your needs

        int width = image.getWidth();
        int height = image.getHeight();

        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;
        int numPixels = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                totalRed += color.getRed();
                totalGreen += color.getGreen();
                totalBlue += color.getBlue();
            }
        }

        int avgRed = (int) (totalRed / numPixels);
        int avgGreen = (int) (totalGreen / numPixels);
        int avgBlue = (int) (totalBlue / numPixels);

        return new Color(avgRed, avgGreen, avgBlue);
    }
}