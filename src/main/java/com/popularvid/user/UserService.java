package com.popularvid.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.popularvid.youtube.YouTubeJob;
import com.popularvid.youtube.YouTubeMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

/**
 * Provides services for other modules that want to interact with user module.
 *
 * Scheduled jobs to fetch trending videos for users is launched also from here.
 *
 * @author Otar Magaldadze
 */
@Service
public class UserService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final UserDao userDao;

    private final Algorithm algorithm;

    public final static String issuer = "Random_issuer";

    private final ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(200);

    private final ConcurrentHashMap<String, ScheduledFuture<?>> jobList = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, YouTubeMsg> youtubeData = new ConcurrentHashMap<>();

    /**
     * Constructs user service class with given user database access object.
     *
     * @param userDao user database access object (auto-injected).
     */
    public UserService(UserDao userDao) {
        this.userDao = userDao;

        byte[] secret;
        try {
            SecureRandom random = SecureRandom.getInstance("NativePRNGNonBlocking");
            secret = new byte[20];
            random.nextBytes(secret);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        algorithm = Algorithm.HMAC256(secret);

        threadPool.setRemoveOnCancelPolicy(true);
    }

    /**
     * Provides user object from database with given username.
     *
     * @param username username string.
     * @return user database object.
     */
    public UserDbo getUser(String username) {
        return userDao.getUser(username);
    }

    /**
     * Method for creating a new user in database.
     *
     * @param user user database object.
     * @return true on success, false otherwise.
     */
    public boolean addUser(UserDbo user) {
        return userDao.addUser(user);
    }

    /**
     * Generate a security token for the given id (username).
     *
     * @param id user id (username in this case).
     * @param expiry expiry time.
     * @return security token.
     */
    String generateToken(String id, Instant expiry)
    {
        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(Date.from(expiry))
                .withClaim("id", id)
                .sign(algorithm);
    }

    /**
     * Get algorithm used in token generation.
     *
     * @return the Algorithm instance.
     */
    public Algorithm getAlgorithm()
    {
        return algorithm;
    }

    /**
     * User authentication method.
     *
     * @param username username string.
     * @param password password string.
     * @return user data transfer object.
     */
    UserDto login(String username, String password) {
        var user = getUser(username);

        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Wrong username or password!");
        }

        Instant expiry = Instant.now().plus(30, ChronoUnit.DAYS);
        String token = generateToken(username, expiry);

        addBackgroundTask(user);

        return new UserDto(user.getUsername(), "", user.getTimeInterval(),
                user.getCountry(), token);
    }

    /**
     * Method to update a time interval of the logged in user.
     *
     * @param timeInterval time interval must be between 1 and 60 minutes.
     * @return updated user data transfer object.
     */
    public UserDto updateTimeInterval(int timeInterval) {
        String username = getCurrentUsername();

        if (username == null) {
            throw new SecurityException("User authentication problem!");
        }

        var user = userDao.updateTimeInterval(username, timeInterval);

        updateTask(user);

        return new UserDto(user.getUsername(), "", user.getTimeInterval(),
                user.getCountry(), "");
    }

    /**
     * Update country of the logged in user.
     *
     * @param country country name string.
     * @return updated user data transfer object.
     */
    public UserDto updateCountry(String country) {
        String username = getCurrentUsername();

        if (username == null) {
            throw new SecurityException("User authentication problem!");
        }

        var user = userDao.updateCountry(username, country);

        updateTask(user);

        return new UserDto(user.getUsername(), "", user.getTimeInterval(),
                user.getCountry(), "");
    }

    /**
     * Method for getting username of a logged in user.
     *
     * For internal use.
     *
     * @return username string.
     */
    private String getCurrentUsername()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return null;
        }

        return auth.getName();
    }

    /**
     * Method for getting user data transfer object of a logged in user.
     *
     * @return user data transfer object.
     */
    public UserDto getCurrentUser() {
        var user = getUser(getCurrentUsername());

        return new UserDto(user.getUsername(), null, user.getTimeInterval(),
                user.getCountry(), null);
    }

    /**
     * Log user out.
     *
     * @return true on success, false otherwise.
     */
    public boolean logout() {
        var username = getCurrentUsername();

        removeBackgroundTask(username);

        return true;
    }

    /**
     * Add youtube video update task for a given user.
     *
     * @param user user database object.
     */
    private void addBackgroundTask(UserDbo user) {
        if (user == null) {
            throw new NullPointerException();
        }

        var scheduledFuture = threadPool.scheduleAtFixedRate(
                new YouTubeJob(user, this),
                0,
                user.getTimeInterval(),
                TimeUnit.MINUTES);

        LOGGER.trace("Adding new background task for user " + user.getUsername());

        jobList.put(user.getUsername(), scheduledFuture);
    }

    /**
     * Remove youtube video update task for a given user.
     *
     * @param username user database object.
     */
    private void removeBackgroundTask(String username) {
        var scheduledFuture = jobList.get(username);

        if (scheduledFuture != null) {
            var ret = scheduledFuture.cancel(true);

            LOGGER.trace(username + " task cancellation result " + ret);
            LOGGER.trace("thread pool size " + threadPool.getPoolSize());

            jobList.remove(username);
        }
    }

    /**
     * A convenience function to remove an old and add a new task.
     *
     * Used when user updates country or time interval setting.
     *
     * @param user user database object.
     */
    private void updateTask(UserDbo user) {
        if (user == null) {
            throw new NullPointerException();
        }

        LOGGER.trace("Update task for user " + user.getUsername());

        removeBackgroundTask(user.getUsername());

        addBackgroundTask(user);
    }

    /**
     * Update cache for user's youtube data.
     *
     * @param username username whose data will be updated.
     * @param msg youtube api response with a video id and a comment string.
     */
    public void updateYoutubeMsg(String username, YouTubeMsg msg) {
        youtubeData.put(username, msg);
    }

    /**
     * Get user's youtube from the cache.
     *
     * @return the video id and a comment string.
     */
    public YouTubeMsg getYouTubeMsg() {
        return youtubeData.get(getCurrentUsername());
    }
}
