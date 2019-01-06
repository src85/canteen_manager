package com.example.canteenchecker.canteenmanager.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.canteenchecker.canteenmanager.R;
import com.example.canteenchecker.canteenmanager.core.ReviewData;
import com.example.canteenchecker.canteenmanager.proxy.ServiceProxy;
import com.example.canteenchecker.canteenmanager.service.MyFirebaseMessagingService;

import java.io.IOException;
import java.text.NumberFormat;

public class ReviewsFragment extends Fragment {

    private static final String TAG = ReviewsFragment.class.toString();
    private static final String CANTEEN_ID_KEY = "canteenId";

    public static Fragment create(String canteenId) {
        ReviewsFragment reviewsFragment = new ReviewsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(CANTEEN_ID_KEY, canteenId);
        reviewsFragment.setArguments(arguments);
        return reviewsFragment;
    }

    private TextView txvAverageRating;
    private RatingBar rtbAverageRating;
    private TextView txvTotalRatings;
    private View viwRatingOne;
    private View viwRatingTwo;
    private View viwRatingThree;
    private View viwRatingFour;
    private View viwRatingFive;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String canteenId = getCanteenId();
            if (canteenId != null && canteenId.equals(MyFirebaseMessagingService.extractCanteenId(intent))) {
                updateReviews();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        txvAverageRating = view.findViewById(R.id.txvAverageRating);
        rtbAverageRating = view.findViewById(R.id.rtbAverageRating);
        txvTotalRatings = view.findViewById(R.id.txvTotalRatings);
        viwRatingOne = view.findViewById(R.id.viwRatingsOne);
        viwRatingTwo = view.findViewById(R.id.viwRatingsTwo);
        viwRatingThree = view.findViewById(R.id.viwRatingsThree);
        viwRatingFour = view.findViewById(R.id.viwRatingsFour);
        viwRatingFive = view.findViewById(R.id.viwRatingsFive);

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver, MyFirebaseMessagingService.createCanteenChangedIntentFilter());

        updateReviews();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    private String getCanteenId() {
        return getArguments().getString(CANTEEN_ID_KEY);
    }

    private void updateReviews() {
        new AsyncTask<String, Void, ReviewData>() {
            @Override
            protected ReviewData doInBackground(String... strings) {
                try {
                    return new ServiceProxy().getReviewsDataForCanteen(strings[0]);
                } catch (IOException e) {
                    Log.e(TAG, "");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ReviewData reviewData) {
                if (reviewData != null) {
                    txvAverageRating.setText(NumberFormat.getNumberInstance().format(reviewData.getAverageRating()));
                    txvTotalRatings.setText(NumberFormat.getNumberInstance().format(reviewData.getTotalRatings()));
                    rtbAverageRating.setRating(reviewData.getAverageRating());
                    setWeight(viwRatingOne, reviewData.getRatingsOne(), reviewData.getTotalRatingsOfMostCommonGrade());
                    setWeight(viwRatingTwo, reviewData.getRatingsTwo(), reviewData.getTotalRatingsOfMostCommonGrade());
                    setWeight(viwRatingThree, reviewData.getRatingsThree(), reviewData.getTotalRatingsOfMostCommonGrade());
                    setWeight(viwRatingFour, reviewData.getRatingsFour(), reviewData.getTotalRatingsOfMostCommonGrade());
                    setWeight(viwRatingFive, reviewData.getRatingsFive(), reviewData.getTotalRatingsOfMostCommonGrade());
                } else {
                    txvAverageRating.setText(null);
                    txvTotalRatings.setText(null);
                    rtbAverageRating.setRating(0);
                    setWeight(viwRatingOne, 0, 1);
                    setWeight(viwRatingTwo, 0, 1);
                    setWeight(viwRatingThree, 0, 1);
                    setWeight(viwRatingFour, 0, 1);
                    setWeight(viwRatingFive, 0, 1);
                }
            }

            private void setWeight(View view, int value, int maximum) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                float weight = ((float) value) / maximum;
                view.setLayoutParams(new LinearLayout.LayoutParams(layoutParams.width, layoutParams.height, weight));
            }
        }.execute(getCanteenId());
    }

}
