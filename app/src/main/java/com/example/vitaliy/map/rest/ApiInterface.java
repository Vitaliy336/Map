package com.example.vitaliy.map.rest;


import com.example.vitaliy.map.model.Detail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Vitaliy on 8/6/2017.
 */

public interface ApiInterface {
    @GET("/service.php")
    Call<Detail> CathcDetail (@Query("location") String location);
}