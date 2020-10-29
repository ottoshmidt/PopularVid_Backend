package com.popularvid.user;

/**
 * User data transfer object.
 *
 * It is a truncated version of database object that hides some of the fields,
 * and also adds a security token.
 *
 * @author Otar Magaldadze
 */
public class UserDto extends UserDbo {

    private final String token;

    /**
     * Constructs a user data transfer object with given parameters.
     *
     * @param username username string.
     * @param password password string.
     * @param timeInterval video update time interval.
     * @param country country name string.
     * @param token security token.
     */
    public UserDto(String username, String password, int timeInterval, String country, String token) {
        super(username, password, timeInterval, country);
        this.token = token;
    }

    /**
     * Get security token.
     *
     * @return security token string.
     */
    public String getToken() {
        return token;
    }
}
