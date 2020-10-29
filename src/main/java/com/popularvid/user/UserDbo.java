package com.popularvid.user;

import java.util.ArrayList;
import java.util.List;

/**
 * User database entity class.
 *
 * It corresponds to a database table of a user.
 *
 * @author Otar Magaldadze
 */
public class UserDbo {

    private final String username;
    private final String password;
    private int timeInterval;
    private String country;

    /**
     * Creates user database object with given parameters.
     *
     * @param username username string.
     * @param password password string.
     * @param timeInterval time interval must be between 1 and 60 minutes.
     * @param country country name as string.
     */
    public UserDbo(String username, String password, int timeInterval, String country) {
        this.username = username.toLowerCase();
        this.password = password;
        this.timeInterval = timeInterval;
        this.country = country;
    }

    /**
     * Username getter.
     *
     * @return username string.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Password getter.
     *
     * @return password string.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Video update time interval getter.
     *
     * @return time interval.
     */
    public int getTimeInterval() {
        return timeInterval;
    }

    /**
     * Video update time interval setter.
     *
     * @param timeInterval time interval must be between 1 and 60 minutes.
     */
    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    /**
     * Country getter.
     *
     * @return country name string.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Country setter.
     *
     * @param country country name string.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns user database object as a list.
     *
     * @return list of user parameters.
     */
    public List<String> toList() {
        var result = new ArrayList<String>(4);
        result.add(username);
        result.add(password);
        result.add(Integer.toString(timeInterval));
        result.add(country);

        return result;
    }

    @Override
    public String toString() {
        return "UserDbo{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", timeInterval=" + timeInterval +
                ", country='" + country + '\'' +
                '}';
    }
}
