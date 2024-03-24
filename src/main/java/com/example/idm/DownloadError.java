package com.example.idm;

public enum DownloadError {
    SUCCESS(0, "Download successful!"),
    CONNECTION_TIMEOUT(1, "Connection interrupted, timeout (but something was read)"),
    NOT_FOUND(2, "Not found (FileNotFoundException) (404)"),
    SERVER_ERROR(3, "Server error (500...)"),
    SOCKET_TIMEOUT(4, "Could not connect: connection timeout (no internet?) java.net.SocketTimeoutException"),
    CONNECT_EXCEPTION(5, "Could not connect: (server down?) java.net.ConnectException"),
    RESOLVE_HOST_ERROR(6, "Could not resolve host (bad host, or no internet - no dns)"),
    UNKNOWN_ERROR(-1, "Unknown error occurred."),
    URL_EMPTY(-2, "URL is empty."),
    URL_INVALID(-3, "URL is invalid.");

    private final int code;
    private final String message;

    DownloadError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static DownloadError getByCode(int code) {
        for (DownloadError error : DownloadError.values()) {
            if (error.getCode() == code) {
                return error;
            }
        }
        return UNKNOWN_ERROR;
    }
}