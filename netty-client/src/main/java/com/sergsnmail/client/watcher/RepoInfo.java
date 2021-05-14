package com.sergsnmail.client.watcher;

public class RepoInfo {
    private long registered;
    private WatchedFileState state;
    private long lastUpdate;

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }

    public WatchedFileState getState() {
        return state;
    }

    public void setState(WatchedFileState state) {
        this.state = state;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "RepoInfo{" +
                "registered=" + registered +
                ", state=" + state +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
