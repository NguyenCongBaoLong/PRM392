package com.prm392.model;

public class Feature {
    private String title;
    private Class<?> targetActivity;

    public Feature(String title, Class<?> targetActivity) {
        this.title = title;
        this.targetActivity = targetActivity;
    }

    public String getTitle() {
        return title;
    }

    public Class<?> getTargetActivity() {
        return targetActivity;
    }
}
