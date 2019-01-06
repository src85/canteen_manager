package com.example.canteenchecker.canteenmanager.core;

import java.util.Collection;

public class Canteen {

    private final String id;
    private String name;

    private String address;
    private String phoneNumber;
    private String website;

    private String meal;
    private float mealPrice;

    private float averageRating;
    private int averageWaitingTime;

    Collection<Rating> ratings;

    public Canteen(String id, String name, String address, String phoneNumber, String website,
                   String meal, float mealPrice, float averageRating,
                   int averageWaitingTime, Collection<Rating> ratings) {

        this.id = id;
        this.name = name;

        this.address = address;
        this.phoneNumber = phoneNumber;
        this.website = website;

        this.meal = meal;
        this.mealPrice = mealPrice;

        this.averageRating = averageRating;
        this.averageWaitingTime = averageWaitingTime;

        this.ratings = ratings;
    }

    /**
     * Getter
     **/
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public String getMeal() {
        return meal;
    }

    public float getMealPrice() {
        return mealPrice;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public int getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public Collection<Rating> getRatings() {
        return ratings;
    }

    /**
     * Setter with logic
     **/
    public void setName(String name) {
        if (!name.isEmpty()) {
            this.name = name;
        }
    }

    public void setAddress(String address) {
        if (!address.isEmpty()) {
            this.address = address;
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        if (!phoneNumber.isEmpty()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void setWebsite(String website) {
        if (!website.isEmpty()) {
            this.website = website;
        }
    }

    public void setMeal(String meal) {
        if (!meal.isEmpty()) {
            this.meal = meal;
        }
    }

    public void setMealPrice(float mealPrice) {
        if (mealPrice > 0) {
            this.mealPrice = mealPrice;
        }
    }

    public void setAverageRating(float averageRating) {
        if (averageRating > 0) {
            this.averageRating = averageRating;
        }
    }

    public void setAverageWaitingTimeWaitingTime(int waitingTime) {
        if (waitingTime > 0) {
            this.averageWaitingTime = waitingTime;
        }
    }

}