package util;

import java.util.regex.Pattern;

/**
 * ValidationUtil - Input validation utilities for registration and login forms.
 * Provides methods for validating email, password, phone, IC, and other user input.
 * Used by Servlets and backing beans for input field validation.
 */
public class ValidationUtil {

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{7,15}$"  // +1234567890 or 1234567890, 7-15 digits
    );

    private static final Pattern IC_PATTERN = Pattern.compile(
            "^[0-9]{6}-[0-9]{2}-[0-9]{4}$|^[0-9]{12}$"  // Malaysian IC format or simple numeric
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z\\s]{2,100}$"  // Letters and spaces, 2-100 chars
    );

    private static final Pattern GENDER_PATTERN = Pattern.compile(
            "^(M|F|Other)$"  // M, F, or Other
    );

    /**
     * Validate email format.
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate password strength.
     * Requirements: At least 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special char
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // At least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // At least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // At least one digit
        if (!password.matches(".*[0-9].*")) {
            return false;
        }
        // At least one special character
        return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    /**
     * Validate phone number format.
     * @param phone the phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate IC/ID number format.
     * Accepts Malaysian IC format (123456-12-1234) or simple 12-digit format
     * @param ic the IC number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIC(String ic) {
        if (ic == null || ic.isBlank()) {
            return false;
        }
        return IC_PATTERN.matcher(ic.trim()).matches();
    }

    /**
     * Validate name format (letters and spaces only).
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate gender value.
     * @param gender the gender to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return false;
        }
        return GENDER_PATTERN.matcher(gender.trim()).matches();
    }

    /**
     * Validate address (non-empty, up to 255 characters).
     * @param address the address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        String trimmed = address.trim();
        return trimmed.length() >= 5 && trimmed.length() <= 255;
    }

    /**
     * Validate that two passwords match.
     * @param password1 the first password
     * @param password2 the second password
     * @return true if they match, false otherwise
     */
    public static boolean passwordsMatch(String password1, String password2) {
        if (password1 == null || password2 == null) {
            return false;
        }
        return password1.equals(password2);
    }

    /**
     * Get user-friendly error message for validation failure.
     * @param fieldName the field that failed validation
     * @return error message
     */
    public static String getErrorMessage(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "email":
                return "Invalid email format. Use: example@domain.com";
            case "password":
                return "Password must be at least 8 characters with uppercase, lowercase, digit, and special character";
            case "phone":
                return "Invalid phone format. Use 7-15 digits, may start with +";
            case "ic":
                return "Invalid IC format. Use format: 123456-12-1234 or 123456121234";
            case "name":
                return "Name must contain only letters and spaces (2-100 characters)";
            case "gender":
                return "Gender must be Male, Female, or Other";
            case "address":
                return "Address must be between 5 and 255 characters";
            case "passwordmatch":
                return "Passwords do not match";
            default:
                return "Invalid input for: " + fieldName;
        }
    }
}
