package com.haircloud.utils

import com.haircloud.data.model.ReviewResponse

fun calculateRatingCounts(reviews: List<ReviewResponse>): Map<Int, Int> {
    val ratingCounts = mutableMapOf<Int, Int>()
    for (i in 1..5) {
        ratingCounts[i] = 0
    }

    reviews.forEach { review ->
        val rating = review.calificacion
        if (rating in 1..5) {
            ratingCounts[rating] = ratingCounts[rating]!! + 1
        }
    }

    return ratingCounts
}