package com.popularvid.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popularvid.user.UserDbo;
import com.popularvid.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Class describes a thread communicating with YouTube API.
 *
 * Fetches the most popular video in the user's country and
 * a top comment to that video. Fetched data is then fed to
 * {@link com.popularvid.user.UserService} to store in a memory cache.
 *
 * @author Otar Magaldadze
 */
public class YouTubeJob implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private final UserDbo user;

    private final UserService userService;

    private final String API_KEY = "AIzaSyDq55A4nDm__N9vaxGX-pY-z7F3TQaMIMU";

    /**
     * Creates a YouTubeJob object with given user and user service.
     *
     * @param user user database object.
     * @param userService user service.
     */
    public YouTubeJob(UserDbo user, UserService userService) {
        this.user = user;
        this.userService = userService;
    }

    @Override
    public void run() {
        LOGGER.trace("Entry");

        String videoId = getPopularVideoID(user.getCountry());
        LOGGER.trace("Video ID " + videoId);

        String comment = getTopComment(videoId);
        LOGGER.trace("Comment: " + comment);

        userService.updateYoutubeMsg(user.getUsername(), new YouTubeMsg(videoId, comment));

        LOGGER.trace("Sleeping for " + user.getTimeInterval() + " minutes.");
    }

    /**
     * Get popular video id from youtube api.
     *
     * @param country country of interest.
     * @return youtube video id.
     */
    private String getPopularVideoID(String country) {
        LOGGER.trace("Entry");

        RestTemplate restTemplate = new RestTemplate();

        String fmtString = "https://www.googleapis.com/youtube/v3/videos?" +
                "chart=mostPopular&regionCode=%s&key=%s";

        String endpoint = String.format(fmtString, country, API_KEY);

        var response = restTemplate.getForObject(endpoint, String.class);

        return parseJsonForVideoId(response);
    }

    /**
     * Get video id from the youtube api json response.
     *
     * @param jsonResponse json data.
     * @return video id.
     */
    private String parseJsonForVideoId(String jsonResponse) {
        LOGGER.trace("Entry");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode itemsNode = objectMapper.readTree(jsonResponse);

            for (JsonNode node : itemsNode.get("items")) {
                return node.get("id").asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetch top comment under the given video.
     *
     * @param videoId youtube video id.
     * @return comment text.
     */
    private String getTopComment(String videoId) {
        LOGGER.trace("Entry");

        RestTemplate restTemplate = new RestTemplate();

        String fmtString = "https://www.googleapis.com/youtube/v3/commentThreads?" +
                "videoId=%s&key=%s&part=snippet&order=relevance";

        String endpoint = String.format(fmtString, videoId, API_KEY);

        LOGGER.trace("Comment endpoint " + endpoint);

        var response = restTemplate.getForObject(endpoint, String.class);

        return parseJsonCommentText(response);
    }

    /**
     * Get comment as text from youtube api json response.
     *
     * @param jsonResponse json data.
     * @return top comment as text.
     */
    private String parseJsonCommentText(String jsonResponse) {
        LOGGER.trace("Entry");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode itemsNode = objectMapper.readTree(jsonResponse);

            for (JsonNode node : itemsNode.get("items")) {
                return node.get("snippet")
                        .get("topLevelComment")
                        .get("snippet")
                        .get("textOriginal")
                        .asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
