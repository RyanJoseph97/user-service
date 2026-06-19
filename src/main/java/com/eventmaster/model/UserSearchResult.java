package com.eventmaster.model;

/**
 * Minimal public-facing projection of a User for search results.
 * Deliberately excludes PII (email, location, account status) so that user
 * search does not leak more than is needed to render a result row.
 */
public class UserSearchResult {

    private final String username;
    private final String name;
    private final String profilePictureUrl;

    public UserSearchResult(User user) {
        this.username = user.getUsername();
        this.name = user.getName();
        this.profilePictureUrl = user.getProfilePictureUrl();
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
}
