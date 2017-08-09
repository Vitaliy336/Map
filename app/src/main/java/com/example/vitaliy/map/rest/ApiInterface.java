package com.example.vitaliy.map.rest;


import com.example.vitaliy.map.model.Place;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static android.R.attr.name;

/**
 * Created by Vitaliy on 8/6/2017.
 */

public interface ApiInterface {
    @GET("/service.php")
    Call<List<Place>> CathcDetail(@Query("location") String location, @Query("name") String name);
}
