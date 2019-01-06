package com.example.canteenchecker.canteenmanager.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canteenchecker.canteenmanager.CanteenManagerApplication;
import com.example.canteenchecker.canteenmanager.R;
import com.example.canteenchecker.canteenmanager.core.IClickedListener;
import com.example.canteenchecker.canteenmanager.core.Rating;
import com.example.canteenchecker.canteenmanager.proxy.ServiceProxy;
import com.example.canteenchecker.canteenmanager.service.MyFirebaseMessagingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RatingsFragment extends Fragment {

    private static final String TAG = RatingsFragment.class.toString();
    private static final String CANTEEN_ID_KEY = "CanteenId";
    private static final int LOGIN_FOR_REVIEW_DELETION = 2210;

    public static Fragment create(String canteenId) {
        RatingsFragment ratingsFragment = new RatingsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(CANTEEN_ID_KEY, canteenId);
        ratingsFragment.setArguments(arguments);
        return ratingsFragment;
    }

    private RatingsAdapter ratingsAdapter = new RatingsAdapter(new IClickedListener() {
        @Override
        public void onClicked(String id) {
            deleteRating(id);
        }
    });

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String canteenId = getCanteenId();
            if (canteenId != null && canteenId.equals(MyFirebaseMessagingService.extractCanteenId(intent))) {
                updateRatings();
            }
        }
    };

    private SwipeRefreshLayout srlRatings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manage_ratings, container, false);

        getChildFragmentManager().beginTransaction().replace(R.id.lnlReviews,
                ReviewsFragment.create(getCanteenId())).commit();

        RecyclerView rcvRatings = view.findViewById(R.id.rcvRatings);
        rcvRatings.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcvRatings.setAdapter(ratingsAdapter);

        srlRatings = view.findViewById(R.id.srlRatings);
        srlRatings.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRatings();
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,
                MyFirebaseMessagingService.createCanteenChangedIntentFilter());

        updateRatings();

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

    private void updateRatings() {
        srlRatings.setRefreshing(true);
        new AsyncTask<Void, Void, Collection<Rating>>() {
            @Override
            protected Collection<Rating> doInBackground(Void... voids) {
                try {
                    Log.v(TAG, getString(R.string.msg_LoadingRatings));
                    return new ServiceProxy().getRatings();
                } catch (IOException e) {
                    Log.e(TAG, getString(R.string.msg_LoadingRatingsFailed), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Collection<Rating> ratings) {
                Collections.sort((ArrayList<Rating>) ratings, Collections.reverseOrder());
                ratingsAdapter.displayRatings(ratings);
                srlRatings.setRefreshing(false);
            }
        }.execute();
    }

    private void deleteRating(final String ratingId) {
        if (CanteenManagerApplication.getInstance().isAuthenticated()) {
            //authenticated
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete_rating, null);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlgDeleteRating_title)
                    .setView(view)
                    .setNegativeButton(R.string.dlgDeleteRating_negativeButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.dlgDeleteRating_positiveButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new AsyncTask<String, Void, Boolean>() {
                                @Override
                                protected Boolean doInBackground(String... strings) {
                                    try {
                                        return new ServiceProxy().deleteRating(strings[0], ratingId);
                                    } catch (IOException e) {
                                        Log.e(TAG, "Review creation failed", e);
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Boolean b) {
                                    Toast.makeText(getActivity(),
                                            b ? getString(R.string.msg_ratingDeleted) : getString(R.string.msg_ratingNotDeleted),
                                            Toast.LENGTH_SHORT).show();
                                    updateRatings();

                                }
                            }.execute(
                                    CanteenManagerApplication.getInstance().getAuthenticationToken()
                            );

                        }
                    })
                    .create()
                    .show();
        } else {
            //not authenticated
            startActivityForResult(LoginActivity.createIntent(getActivity()), LOGIN_FOR_REVIEW_DELETION);
        }
    }

    private static class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.ViewHolder> {

        private IClickedListener clickedListener;

        public RatingsAdapter(IClickedListener cl) {
            super();
            if (cl != null) {
                clickedListener = cl;
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView txvUserName = itemView.findViewById(R.id.txvUserName);
            private final TextView txvRemark = itemView.findViewById(R.id.txvRemark);
            private final RatingBar rtbRating = itemView.findViewById(R.id.rtbRating);
            private final TextView txvTimestamp = itemView.findViewById(R.id.txvTimestamp);
            private final Button btnDelete = itemView.findViewById(R.id.btnDelete);

            private IClickedListener clickedListener;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_rating_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RatingsAdapter.ViewHolder holder, int position) {
            final Rating r = ratingList.get(position);

            holder.txvUserName.setText(r.getUserName());
            holder.txvRemark.setText(r.getRemark());
            holder.rtbRating.setRating(r.getRatingPoints());
            holder.txvTimestamp.setText(r.getDate());
            holder.clickedListener = clickedListener;

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.clickedListener != null) {
                        holder.clickedListener.onClicked(r.getId());
                    }
                }
            });
        }

        private final List<Rating> ratingList = new ArrayList<>();

        void displayRatings(Collection<Rating> ratings) {
            ratingList.clear();
            if (ratings != null) {
                ratingList.addAll(ratings);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return ratingList.size();
        }
    }

}
