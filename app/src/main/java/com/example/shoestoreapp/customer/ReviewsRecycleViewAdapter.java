package com.example.shoestoreapp.customer;

import android.content.Context;
import android.media.Rating;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.shoestoreapp.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ReviewsRecycleViewAdapter extends RecyclerView.Adapter<ReviewsRecycleViewAdapter.ViewHolder> {

    private ArrayList<String> mNames, mReviews, mRatings;
    private Context mContext;

    public ReviewsRecycleViewAdapter(Context mContext, ArrayList<String> mNames, ArrayList<String> mReviews, ArrayList<String> mRatings) {
        this.mNames = mNames;
        this.mReviews = mReviews;
        this.mRatings = mRatings;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_list_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.userName.setText(mNames.get(position));
        holder.userReview.setText(mReviews.get(position));
        holder.ratingNumber.setText(mRatings.get(position));
        holder.userRating.setRating(Float.parseFloat(mRatings.get(position)));
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView userName, ratingNumber, userReview;
        RatingBar userRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.buyerFullNameTextView);
            ratingNumber = itemView.findViewById(R.id.ratingNumberTextView);
            userRating = itemView.findViewById(R.id.userRatingBar);
            userReview = itemView.findViewById(R.id.buyerReviewTextView);
        }
    }
}
