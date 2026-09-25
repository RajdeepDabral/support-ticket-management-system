package com.supportticket.support;

public final class IntegrationTestSupport {

    public static final String TICKETS_URL = "/api/v1/tickets";

    private IntegrationTestSupport() {
    }

    public static String ticketUrl(Long ticketId) {
        return TICKETS_URL + "/" + ticketId;
    }

    public static String statusUrl(Long ticketId) {
        return ticketUrl(ticketId) + "/status";
    }

    public static String commentsUrl(Long ticketId) {
        return ticketUrl(ticketId) + "/comments";
    }
}
