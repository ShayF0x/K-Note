package fr.shayfox.k_note;

import org.junit.Test;

import static org.junit.Assert.*;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import fr.shayfox.k_note.controller.MainActivity;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    private void checkUpdate() {
        String version = "1.0";

        System.out.println("Version: "+version);

        try {
            System.out.println("connection");
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/ShayF0x/K-Note/releases/latest").openConnection();
            //connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                System.out.println("error");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF8")))) {
                System.out.println(reader);
            } catch (JsonSyntaxException | NumberFormatException ex) {
                System.out.println("Failed to parse the latest version info.");
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            System.out.println("LOG: Failed to get release info from api.github.com.");
            ex.printStackTrace();
        }
    }
}