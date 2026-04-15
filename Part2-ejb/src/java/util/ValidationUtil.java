package util;

import java.util.Locale;
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

    private static final Pattern IC_PATTERN = Pattern.compile(
            "^[0-9]{6}-[0-9]{2}-[0-9]{4}$|^[0-9]{12}$"  // Malaysian IC format or simple numeric
    );

    private static final Pattern PHONE_ALLOWED_PATTERN = Pattern.compile(
            "^[0-9\\-\\s]+$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z][A-Za-z '\\-]{1,99}$"  // Letters, spaces, apostrophes, hyphens
    );

    private static final Pattern GENDER_PATTERN = Pattern.compile(
            "^(M|F|Other)$"  // M, F, or Other
    );

    private static final Pattern SPECIALTY_PATTERN = Pattern.compile(
            "^[A-Za-z][A-Za-z &'/\\-]{1,99}$"
    );

    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile(
            "^[A-Za-z0-9][A-Za-z0-9 &'/()\\-]{1,99}$"
    );

    private ValidationUtil() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    public static String normalizeIC(String ic) {
        String trimmed = trimToNull(ic);
        return trimmed == null ? null : trimmed.replaceAll("\\D", "");
    }

    public static String normalizePhone(String phone) {
        String trimmed = trimToNull(phone);
        if (trimmed == null || !PHONE_ALLOWED_PATTERN.matcher(trimmed).matches()) {
            return null;
        }

        String digitsOnly = trimmed.replaceAll("\\D", "");
        if (digitsOnly.matches("^011\\d{8}$") || digitsOnly.matches("^01\\d\\d{7}$")) {
            return digitsOnly.substring(0, 3) + "-" + digitsOnly.substring(3);
        }

        return null;
    }

    /**
     * Validate email format.
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(normalizedEmail).matches();
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
        String normalizedPhone = normalizePhone(phone);
        return normalizedPhone != null;
    }

    /**
     * Validate IC/ID number format.
     * Accepts Malaysian IC format (123456-12-1234) or simple 12-digit format
     * @param ic the IC number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIC(String ic) {
        String trimmedIc = trimToNull(ic);
        if (trimmedIc == null) {
            return false;
        }
        return IC_PATTERN.matcher(trimmedIc).matches();
    }

    /**
     * Validate name format (letters and spaces only).
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        String trimmedName = trimToNull(name);
        if (trimmedName == null) {
            return false;
        }
        return NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validate gender value.
     * @param gender the gender to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGender(String gender) {
        String trimmedGender = trimToNull(gender);
        if (trimmedGender == null) {
            return false;
        }
        return GENDER_PATTERN.matcher(trimmedGender).matches();
    }

    /**
     * Validate address (non-empty, up to 255 characters).
     * @param address the address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAddress(String address) {
        String trimmed = trimToNull(address);
        if (trimmed == null) {
            return false;
        }
        return trimmed.length() >= 5 && trimmed.length() <= 255;
    }

    public static boolean isValidSpecialty(String specialty) {
        String trimmedSpecialty = trimToNull(specialty);
        if (trimmedSpecialty == null) {
            return false;
        }
        return SPECIALTY_PATTERN.matcher(trimmedSpecialty).matches();
    }

    public static boolean isValidServiceName(String serviceName) {
        String trimmedServiceName = trimToNull(serviceName);
        if (trimmedServiceName == null) {
            return false;
        }
        return SERVICE_NAME_PATTERN.matcher(trimmedServiceName).matches();
    }

    public static boolean isValidServiceType(String serviceType) {
        String trimmedServiceType = trimToNull(serviceType);
        if (trimmedServiceType == null) {
            return false;
        }
        return "Normal".equalsIgnoreCase(trimmedServiceType)
                || "Major".equalsIgnoreCase(trimmedServiceType);
    }

    public static boolean isValidServicePrice(Double price) {
        return price != null && price > 0;
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
                return "Email must be in a valid format, for example: example@domain.com.";
            case "password":
                return "Password must be at least 8 characters and include uppercase, lowercase, digit, and special character.";
            case "phone":
                return "Phone must be a valid Malaysian mobile number such as 012-3456789 or 011-12345678. You may enter digits only and the system will format it automatically.";
            case "ic":
                return "IC must be 12 digits, with or without hyphens, for example 900101-01-1111.";
            case "name":
                return "Name must be 2-100 characters and use letters, spaces, apostrophes, or hyphens only.";
            case "gender":
                return "Please select a valid gender.";
            case "address":
                return "Address must be between 5 and 255 characters.";
            case "passwordmatch":
                return "Passwords do not match.";
            case "specialty":
                return "Specialty must be 2-100 characters and use letters, spaces, ampersands, apostrophes, or hyphens only.";
            case "servicename":
                return "Service name must be 2-100 characters and may include letters, numbers, spaces, ampersands, apostrophes, slashes, parentheses, or hyphens.";
            case "servicetype":
                return "Please select either Normal or Major service type.";
            case "serviceprice":
                return "Service price must be greater than 0.";
            default:
                return "Invalid input for: " + fieldName;
        }
    }
}
