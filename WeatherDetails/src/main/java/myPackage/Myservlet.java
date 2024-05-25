package myPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/Myservlet")
public class Myservlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Myservlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String apiKey = "265dcf78e8726af7ca8e18c65cb64ada\r\n"; // Replace with your actual API key
        String city = request.getParameter("city");

        if (city == null || city.isEmpty()) {
            response.getWriter().println("Please provide a city name.");
            return;
        }

      

        try {
        	
        	  String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
        	
        	
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder responseContent = new StringBuilder();
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }
            scanner.close();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

            if (jsonObject.has("cod") && jsonObject.get("cod").getAsInt() == 200) {
                // Data retrieval successful
                long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
                String date = new Date(dateTimestamp).toString();
                double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
                int temperatureCelsius = (int) (temperatureKelvin - 273.15);
                int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
                double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
                String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject()
                        .get("main").getAsString();

                request.setAttribute("date", date);
                request.setAttribute("city", city);
                request.setAttribute("temperature", temperatureCelsius);
                request.setAttribute("weatherCondition", weatherCondition);
                request.setAttribute("humidity", humidity);
                request.setAttribute("windSpeed", windSpeed);
                request.setAttribute("weatherData", responseContent.toString());
            } else {
                // Error occurred, handle it
                String errorMessage = "Error: ";
                if (jsonObject.has("message")) {
                    errorMessage += jsonObject.get("message").getAsString();
                } else {
                    errorMessage += "Unknown error occurred.";
                }
                request.setAttribute("errorMessage", errorMessage);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while connecting to the API.");
        }

        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}