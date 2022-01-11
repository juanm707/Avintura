package com.example.avintura.network


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://api.yelp.com/v3/"
const val API_KEY = "12uNczIb2rFuIg3PFn6kkaHV3twRDKvAQEY6K84TPwVoFFjMPyRjrXlaCLXurgNzX_hQVaUfcFiPr2tVDTXcr5wNRvsLmwKjcQhm3NLop91yK33Htj4Idrr7S15KYXYx"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface YelpApiService {

    @GET("businesses/search")
    suspend fun searchBusinesses(
        @Header("Authorization") authHeader: String = "Bearer $API_KEY",
        @Query("term") searchTerm: String?,
        @Query("location") location: String,
        @Query("offset") offset: Int,
        @Query("sort_by") sortBy: String = "best_match",
        @Query("limit") limit: Int = 50
    ) : YelpBusinessContainer

    @GET("businesses/{id}")
    suspend fun getBusiness(
        @Header("Authorization") authHeader: String = "Bearer $API_KEY",
        @Path("id") id: String
    ) : YelpBusinessDetail

    @GET("businesses/{id}/reviews")
    suspend fun getReviews(
        @Header("Authorization") authHeader: String = "Bearer $API_KEY",
        @Path("id") id: String
    ) : YelpReviewContainer
}

object YelpAPINetwork {
    val retrofitYelpService: YelpApiService by lazy { retrofit.create(YelpApiService::class.java) }
}