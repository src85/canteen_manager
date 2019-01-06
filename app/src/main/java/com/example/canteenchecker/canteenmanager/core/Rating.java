package com.example.canteenchecker.canteenmanager.core;

import java.text.SimpleDateFormat;

public class Rating implements Comparable<Rating> {

    private final String id;
    private final String userName;
    private final String remark;
    private final int ratingPoints;
    private final long timeStamp;
    private final String date;

    public Rating(String id, String userName, String remark, int ratingPoints, long timeStamp) {
        this.id = id;
        this.userName = userName;
        this.remark = remark;
        this.ratingPoints = ratingPoints;

        this.timeStamp = timeStamp;
        this.date = (new SimpleDateFormat("dd.MM.yyyy HH:mm"))
                .format(new java.util.Date(timeStamp));
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getRemark() {
        return remark;
    }

    public int getRatingPoints() {
        return ratingPoints;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(Rating other) {
        if (other != null) {
            if (other.timeStamp < this.timeStamp) {
                return 1;
            } else if (other.timeStamp > this.timeStamp) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getId());
        sb.append(" ");
        sb.append("(");
        sb.append(getUserName());
        sb.append(")");
        sb.append(": ");
        sb.append(getRemark());

        return sb.toString();
    }
}
