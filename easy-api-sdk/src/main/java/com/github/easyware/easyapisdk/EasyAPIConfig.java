package com.github.easyware.easyapisdk;

public class EasyAPIConfig {
    private String title;
    private String[] serverUrls;
    private String easyAPIAppUrl;
    private String easyAPIAppGroup;
    private long cacheSeconds=60L;
    private long commentCacheSeconds=60L;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getServerUrls() {
        return serverUrls;
    }

    public void setServerUrls(String[] serverUrls) {
        this.serverUrls = serverUrls;
    }

    public String getEasyAPIAppUrl() {
        return easyAPIAppUrl;
    }

    public void setEasyAPIAppUrl(String easyAPIAppUrl) {
        this.easyAPIAppUrl = easyAPIAppUrl;
    }

    public String getEasyAPIAppGroup() {
        return easyAPIAppGroup;
    }

    public void setEasyAPIAppGroup(String easyAPIAppGroup) {
        this.easyAPIAppGroup = easyAPIAppGroup;
    }

    public long getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds(long cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public long getCommentCacheSeconds() {
        return commentCacheSeconds;
    }

    public void setCommentCacheSeconds(long commentCacheSeconds) {
        this.commentCacheSeconds = commentCacheSeconds;
    }
}
