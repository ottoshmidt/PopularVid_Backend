package com.popularvid.youtube;

/**
 * YouTube message json model.
 *
 * @author Otar Magaldadze
 */
@SuppressWarnings("unused")
public class YouTubeMsg {

    private String videoId;
    private String comment;

    /**
     * Create an youtube msg with a video id and a comment text.
     *
     * @param videoId youtube video id.
     * @param comment comment text.
     */
    public YouTubeMsg(String videoId, String comment) {
        this.videoId = videoId;
        this.comment = comment;
    }

    /**
     * Get video id.
     *
     * @return video id string.
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Set video id.
     *
     * @param videoId video id string.
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * Get comment.
     *
     * @return comment text.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set comment text.
     *
     * @param comment comment text.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
