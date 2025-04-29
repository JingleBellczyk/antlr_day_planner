package logic;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for standardized error handling across the application
 */
public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    /**
     * Error categories for better organization and handling
     */
    public enum ErrorCategory {
        AUTHENTICATION("Authentication Error"),
        PARSING("Command Parsing Error"),
        API_COMMUNICATION("API Communication Error"),
        VALIDATION("Input Validation Error"),
        PERMISSION("Permission Error"),
        FILE_ACCESS("File Access Error"),
        UNKNOWN("Unknown Error");

        private final String displayName;

        ErrorCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Handle exceptions in a standardized way
     * @param ex The exception to handle
     * @param context Additional context about where the error occurred
     * @return A list of user-friendly error messages
     */
    public static List<String> handleException(Exception ex, String context) {
        List<String> messages = new ArrayList<>();
        ErrorCategory category = categorizeException(ex);

        // Log the full exception for debugging
        LOGGER.log(Level.WARNING, context + ": " + category.getDisplayName(), ex);

        // Add user-friendly header
        messages.add(category.getDisplayName() + ": " + getUserFriendlyMessage(ex, category));

        // Add recovery suggestions
        messages.addAll(getRecoverySuggestions(category));

        return messages;
    }

    /**
     * Categorize an exception to determine its type
     */
    private static ErrorCategory categorizeException(Exception ex) {
        if (ex instanceof java.net.UnknownHostException ||
                ex instanceof java.net.ConnectException) {
            return ErrorCategory.API_COMMUNICATION;
        } else if (ex instanceof java.io.FileNotFoundException ||
                ex instanceof java.nio.file.NoSuchFileException) {
            return ErrorCategory.FILE_ACCESS;
        } else if (ex instanceof com.google.api.client.auth.oauth2.TokenResponseException ||
                ex instanceof java.security.GeneralSecurityException) {
            return ErrorCategory.AUTHENTICATION;
        } else if (ex instanceof IllegalArgumentException ||
                ex instanceof java.text.ParseException) {
            return ErrorCategory.VALIDATION;
        } else if (ex instanceof com.google.api.client.googleapis.json.GoogleJsonResponseException) {
            // Check if it's a permissions issue
            if (ex.getMessage().contains("403") ||
                    ex.getMessage().contains("permission")) {
                return ErrorCategory.PERMISSION;
            }
            return ErrorCategory.API_COMMUNICATION;
        } else if (ex instanceof org.antlr.v4.runtime.RecognitionException) {
            return ErrorCategory.PARSING;
        }

        return ErrorCategory.UNKNOWN;
    }

    /**
     * Get a user-friendly error message
     */
    private static String getUserFriendlyMessage(Exception ex, ErrorCategory category) {
        switch (category) {
            case AUTHENTICATION:
                return "Unable to authenticate with Google services. Your credentials may be expired.";
            case PARSING:
                return "Invalid command syntax. Check your command format.";
            case API_COMMUNICATION:
                return "Failed to communicate with Google services. Please check your internet connection.";
            case VALIDATION:
                return "Input validation failed: " + ex.getMessage();
            case PERMISSION:
                return "You don't have permission to perform this action.";
            case FILE_ACCESS:
                return "Unable to access the specified file: " + ex.getMessage();
            case UNKNOWN:
            default:
                return ex.getMessage();
        }
    }

    /**
     * Get recovery suggestions based on error category
     */
    private static List<String> getRecoverySuggestions(ErrorCategory category) {
        List<String> suggestions = new ArrayList<>();

        switch (category) {
            case AUTHENTICATION:
                suggestions.add("Try refreshing your Google API credentials.");
                suggestions.add("Run 'help authentication' for steps to refresh your token.");
                break;
            case PARSING:
                suggestions.add("Try 'help' to see the correct command syntax.");
                suggestions.add("Check command examples with 'mail help' or 'calendar help'.");
                break;
            case API_COMMUNICATION:
                suggestions.add("Check your internet connection.");
                suggestions.add("Google services might be experiencing downtime. Try again later.");
                break;
            case VALIDATION:
                suggestions.add("Make sure dates are in format DD.MM.YYYY and times in HH:MM.");
                suggestions.add("Enclose text arguments in double quotes, e.g. \"My title\".");
                break;
            case PERMISSION:
                suggestions.add("Verify you have the required permissions in your Google account.");
                suggestions.add("Try re-authorizing the application with the required scopes.");
                break;
            case FILE_ACCESS:
                suggestions.add("Check if the file exists at the specified path.");
                suggestions.add("Verify that file paths don't contain unescaped special characters.");
                break;
            default:
                suggestions.add("Try simplifying your command and try again.");
                suggestions.add("Check 'help' for documentation on proper command usage.");
        }

        return suggestions;
    }

}