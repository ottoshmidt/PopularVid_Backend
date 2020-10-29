package com.popularvid.user;

/**
 * User login data transfer object.
 *
 * @author Otar Magaldadze
 */
public class LoginDto {

    private final String username;
    private final String password;

    /**
     * Creates an instance of user login data transfer object.
     *
     * @param username username string.
     * @param password password string.
     */
    public LoginDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Getter of username string.
     *
     * @return username string.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter of password string.
     *
     * @return password string.
     */
    public String getPassword() {
        return password;
    }
}
