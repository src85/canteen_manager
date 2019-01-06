package com.example.canteenchecker.canteenmanager.proxy;

import android.util.Log;

import com.example.canteenchecker.canteenmanager.CanteenManagerApplication;
import com.example.canteenchecker.canteenmanager.R;
import com.example.canteenchecker.canteenmanager.core.Canteen;
import com.example.canteenchecker.canteenmanager.core.Rating;
import com.example.canteenchecker.canteenmanager.core.ReviewData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class ServiceProxy {

    // ADMIN --> https://canteencheckeradmin.azurewebsites.net/
    // Test user: S1610307038/S1610307038
    // SWAGGER --> https://canteenchecker.azurewebsites.net/swagger/ui/index
    private static final String SERVICE_BASE_URL = "https://canteenchecker.azurewebsites.net/";
    private static final String TAG = ServiceProxy.class.toString();

    private final Proxy proxy = new Retrofit.Builder()
            .baseUrl(SERVICE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Proxy.class);

    public Canteen getCanteen() throws IOException {
        if (CanteenManagerApplication.getInstance().isAuthenticated()) {
            String authToken = CanteenManagerApplication.getInstance().getAuthenticationToken();
            ProxyCanteen canteen =
                    proxy.getCanteen(String.format("Bearer %s", authToken)).execute().body();
            return canteen != null ? canteen.toCanteen() : null;
        }
        return null;
    }

    public Collection<Rating> getRatings() throws IOException {
        Canteen canteen = this.getCanteen();
        if (canteen == null) return null;

        ArrayList<Rating> result = new ArrayList<>();
        result.addAll(canteen.getRatings());
        return result.isEmpty() ? null : result;
    }

    public ReviewData getReviewsDataForCanteen(String canteenId) throws IOException {
        ProxyReviewData reviewData = proxy.getReviewDataForCanteen(canteenId).execute().body();
        return reviewData != null ? reviewData.toReviewData() : null;
    }

    public String authenticate(String userName, String password) throws IOException {
        return proxy.postLogin(new ProxyLogin(userName, password)).execute().body();
    }

    public boolean deleteRating(String authToken, String ratingId) throws IOException {
        return proxy.deleteRating(String.format("Bearer %s", authToken), Integer.parseInt(ratingId))
                .execute().isSuccessful();
    }

    public void updateCanteen(String authToken, Canteen canteen) {
        try {
            proxy.putCanteen(String.format("Bearer %s", authToken),
                    new ProxyNewCanteen(canteen.getId(), canteen.getName(),
                            canteen.getAddress(), canteen.getPhoneNumber(), canteen.getWebsite(),
                            canteen.getMeal(), canteen.getMealPrice(), canteen.getAverageRating(),
                            canteen.getAverageWaitingTime())).execute();
        } catch (IOException ex) {
            Log.e(TAG, String.valueOf(R.string.msg_LoadingCanteenFailed), ex);
        }
    }

    private interface Proxy {

        @POST("/Admin/Login")
        Call<String> postLogin(@Body ProxyLogin login);

        @GET("/Public/Canteen/{id}/Rating?nrOfRatings=0")
        Call<ProxyReviewData> getReviewDataForCanteen(@Path("id") String canteenId);

        /**
         * New REST-Methods for CanteenManager:
         **/
        @GET("/Admin/Canteen")
        Call<ProxyCanteen> getCanteen(@Header("Authorization") String authenticationToken);

        @PUT("/Admin/Canteen")
        Call<Void> putCanteen(@Header("Authorization") String authenticationToken, @Body ProxyNewCanteen updateModel);

        @DELETE("/Admin/Canteen/Rating/{id}")
        Call<ResponseBody> deleteRating(@Header("Authorization") String authenticationToken, @Path("id") int ratingId);

    }

    private static class ProxyCanteen {

        int canteenId;
        String name;

        String address;
        String phone;
        String website;

        String meal;
        float mealPrice;

        float averageRating;
        int averageWaitingTime;

        ProxyRating[] ratings;

        ProxyCanteen(String canteenId, String name, String address, String phone,
                     String website, String meal, float mealPrice, float averageRating,
                     int averageWaitingTime, ProxyRating[] ratings) {

            this.canteenId = Integer.parseInt(canteenId);
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.website = website;
            this.meal = meal;
            this.mealPrice = mealPrice;
            this.averageRating = averageRating;
            this.averageWaitingTime = averageWaitingTime;
            this.ratings = ratings;
        }

        Canteen toCanteen() {
            Collection<Rating> ratingList = new ArrayList<>();
            if (ratings != null) {
                for (ProxyRating rating : ratings) {
                    ratingList.add(rating.toRating());
                }
            }
            return new Canteen(String.valueOf(canteenId), name, address, phone, website,
                    meal, mealPrice, averageRating, averageWaitingTime, ratingList);
        }

    }

    private static class ProxyNewCanteen {

        final int canteenId;
        final String name;

        final String address;
        final String phone;
        final String website;

        final String meal;
        final float mealPrice;

        final float averageRating;
        final int averageWaitingTime;

        ProxyNewCanteen(String canteenId, String name, String address, String phone,
                        String website, String meal, float mealPrice, float averageRating,
                        int averageWaitingTime) {

            this.canteenId = Integer.parseInt(canteenId);
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.website = website;
            this.meal = meal;
            this.mealPrice = mealPrice;
            this.averageRating = averageRating;
            this.averageWaitingTime = averageWaitingTime;
        }
    }


    private static class ProxyRating {

        int ratingId;
        String username;
        String remark;
        int ratingPoints;
        long timestamp;

        Rating toRating() {
            return new Rating(String.valueOf(ratingId), username, remark, ratingPoints, timestamp);
        }

    }

    private static class ProxyReviewData {

        float average;
        int totalCount;
        int[] countsPerGrade;

        private int getRatingsForGrade(int grade) {
            grade--;
            return countsPerGrade != null && grade >= 0
                    && grade < countsPerGrade.length ? countsPerGrade[grade] : 0;
        }

        ReviewData toReviewData() {
            return new ReviewData(average, totalCount, getRatingsForGrade(1),
                    getRatingsForGrade(2), getRatingsForGrade(3), getRatingsForGrade(4),
                    getRatingsForGrade(5));
        }

    }

    private static class ProxyLogin {

        final String username;
        final String password;

        ProxyLogin(String userName, String password) {
            this.username = userName;
            this.password = password;
        }

    }
}
