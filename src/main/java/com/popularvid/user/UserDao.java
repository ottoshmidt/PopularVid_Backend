package com.popularvid.user;

import com.popularvid.database.DbInterface;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

//TODO password md5 hash

/**
 * Database access object for a user.
 *
 * Gets and inserts data concerning a user into and from the database.
 * Interacts directly with database interface.
 *
 * @author Otar Magaldadze
 */
@Component
public class UserDao {

    private final DbInterface db;

    private final String TABLE_NAME = "users";

    /**
     * Creates new instance with the given underlying database.
     *
     * @param db underlying database.
     */
    public UserDao(DbInterface db) {
        this.db = db;
    }

    /**
     * Get user database object with the given username as a key.
     *
     * @param username username string.
     * @return user database object.
     */
    UserDbo getUser(String username) {
        List<String> userFields;

        try {
            userFields = db.select(TABLE_NAME, username.toLowerCase());
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        if (userFields.isEmpty()) {
            return null;
        }

        return new UserDbo(userFields.get(0),
                userFields.get(1),
                Integer.parseInt(userFields.get(2)),
                userFields.get(3));
    }

    /**
     * Create new user.
     *
     * All fields are mandatory. Time interval should be between 1 and 60 minutes.
     *
     * @param user user database object.
     * @return true on success, otherwise false.
     */
    boolean addUser(UserDbo user) {
        verifyUserData(user);

        try {
            db.insert(TABLE_NAME, user.toList());
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    /**
     * Internal function to verify data consistency.
     *
     * @param user user database object.
     */
    private void verifyUserData(UserDbo user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username not provided.");
        }

        if (Pattern.matches("\\p{IsPunctuation}", user.getUsername())) {
            throw new IllegalArgumentException("Username must only contain alphanumeric characters.");
        }

        if (Pattern.matches("[^A-Za-z]", String.format("%c", user.getUsername().charAt(0)))) {
            throw new IllegalArgumentException("Username must start with a letter.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password not provided.");
        }

        if (user.getCountry() == null || user.getCountry().isEmpty()) {
            throw new IllegalArgumentException("Country not provided.");
        }

        int timeInterval = user.getTimeInterval();

        if (timeInterval < 1 || timeInterval > 60) {
            throw new IllegalArgumentException("Time interval should be between 1-60!");
        }
    }

    /**
     * Edit video update time interval for the given user.
     *
     * @param username username string.
     * @param timeInterval time interval should be between 1 and 60 minutes.
     * @return updated user database object.
     */
    UserDbo updateTimeInterval(String username, int timeInterval) {
        if (timeInterval < 1 || timeInterval > 60) {
            throw new IllegalArgumentException("Time interval should be between 1-60!");
        }

        UserDbo currUser = getUser(username);

        if (currUser == null) {
            throw new IllegalArgumentException("User doesn't exist");
        }

        if (timeInterval != currUser.getTimeInterval()) {
            currUser.setTimeInterval(timeInterval);
        }

        try {
            db.update(TABLE_NAME, currUser.toList());
        } catch (IOException e) {
            e.printStackTrace();

            currUser = null;
        }

        return currUser;
    }

    /**
     * Edit country for the given user.
     *
     * @param username username.
     * @param country new country parameter.
     * @return updated user database object.
     */
    UserDbo updateCountry(String username, String country) {
        UserDbo currUser = getUser(username);

        if (currUser == null) {
            throw new IllegalArgumentException("User doesn't exist");
        }

        if (!country.equals(currUser.getCountry())) {
            currUser.setCountry(country);
        }

        try {
            db.update(TABLE_NAME, currUser.toList());
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        return currUser;
    }
}
