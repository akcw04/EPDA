package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Security utility for password hashing.
 * Implements SHA-256 hashing for passwords before storage in database.
 */
public class SecurityUtil {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Hash a password using SHA-256.
     * This is executed in the Business Tier (EJB) before JDBC layer receives it.
     *
     * @param password plain text password
     * @return SHA-256 hashed password (hex string)
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Convert byte array to hexadecimal string.
     *
     * @param bytes array of bytes
     * @return hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Verify if a plain password matches a hashed password.
     *
     * @param plainPassword plain text password to verify
     * @param hashedPassword previously hashed password from database
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashOfInput = hashPassword(plainPassword);
        return hashOfInput.equals(hashedPassword);
    }
}
