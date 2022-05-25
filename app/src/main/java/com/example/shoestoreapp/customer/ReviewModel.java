package com.example.shoestoreapp.customer;

public class ReviewModel {
    String email, review;
    Double rating;

    public ReviewModel(String email, String review, Double rating) {
        this.email = email;
        this.review = review;
        this.rating = rating;
    }

    public ReviewModel(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
    @Override
    public String toString() {
        return "ReviewModel{" +
                "email='" + email + '\'' +
                ", review='" + review + '\'' +
                ", rating=" + rating +
                '}';
    }
}
