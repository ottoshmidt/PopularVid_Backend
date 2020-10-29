package com.popularvid.user;

import com.popularvid.youtube.YouTubeMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.management.openmbean.KeyAlreadyExistsException;

/**
 * HTTP request handler class of the user module.
 *
 * @author Otar Magaldadze
 */
@RestController
@RequestMapping(path="/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger LOGGER = LogManager.getLogger();

    private final UserService userService;

    /**
     * Constructs instance with the given UserService object.
     *
     * @param userService service class for user module (auto-injected).
     */
    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    /**
     * Endpoint for checking if username exists.
     *
     * @param username path variable of a username.
     * @return response entity with boolean body.
     */
    @GetMapping("/user-exists/{username}")
    ResponseEntity<Boolean> userExists(@PathVariable("username") String username)
    {
        return userService.getUser(username) != null ? ResponseEntity.status(HttpStatus.OK).build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint for adding a new user.
     *
     * @param user object holding user data to be created.
     * @return response entity with string body.
     */
    @PostMapping("/add-user")
    ResponseEntity<String> addUser(@RequestBody UserDbo user)
    {
        LOGGER.trace(user);

        try {
            if (userService.addUser(user)) {
                return ResponseEntity.ok("Successfully created!");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (KeyAlreadyExistsException e) {
            LOGGER.debug(e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.debug(e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint for logging users in.
     *
     * @param loginDto login request body.
     * @return response entity with user body.
     */
    @PostMapping("/login")
    ResponseEntity<UserDto> login(@RequestBody LoginDto loginDto)
    {
        try {
            LOGGER.trace(loginDto.getUsername() + " " + loginDto.getPassword());
            return ResponseEntity.ok(userService.login(loginDto.getUsername(), loginDto.getPassword()));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(e.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint for editing video update time intervals for the logged in user.
     *
     * @param timeInterval time interval int minutes between 1 and 60.
     * @return response entity with updated user body.
     */
    @PostMapping("/update-time-interval")
    @Secured("ROLE_USER")
    ResponseEntity<UserDto> updateTimeInterval(@RequestParam int timeInterval)
    {
        try {
            return ResponseEntity.ok(userService.updateTimeInterval(timeInterval));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(e.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            LOGGER.debug(e.getMessage());

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Endpoint for editing country setting for the logged in user.
     *
     * @param country country string.
     * @return response entity with updated user body.
     */
    @PostMapping("/update-country")
    @Secured("ROLE_USER")
    ResponseEntity<UserDto> updateCountry(@RequestParam String country)
    {
        try {
            return ResponseEntity.ok(userService.updateCountry(country));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(e.getMessage());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            LOGGER.debug(e.getMessage());

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Endpoint for getting data of the logged in user.
     *
     * @return response entity with user body.
     */
    @PostMapping("/get-current-user")
    @Secured("ROLE_USER")
    ResponseEntity<UserDto> getCurrentUser()
    {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    /**
     * Endpoint for logging the user out.
     *
     * @return response entity with user body.
     */
    @GetMapping("/logout")
    @Secured("ROLE_USER")
    ResponseEntity<String> logout()
    {
        if (userService.logout()) {
            return ResponseEntity.ok("Logged out successfully!");
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Logout failed!");
        }
    }

    /**
     * Gets most viewed video in the user's country and a top comment
     * of that video.
     *
     * @return video id and a comment text in json format.
     */
    @GetMapping("/get-youtube-msg")
    @Secured("ROLE_USER")
    ResponseEntity<YouTubeMsg> getYouTubeMsg()
    {
        return ResponseEntity.ok(userService.getYouTubeMsg());
    }
}
