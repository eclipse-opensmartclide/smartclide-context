package de.atb.context.monitoring.models;

public enum GitMessageHeader {
    NEW_COMMIT("new commit"),

    NEW_FILE_CHANGED("new file changed");

    private final String header;

    GitMessageHeader(final String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
