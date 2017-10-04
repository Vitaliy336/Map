package com.example.vitaliy.map.rest;


import com.example.vitaliy.map.model.Place;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Vitaliy on 8/6/2017.
 */

public interface ApiInterface {
    @GET("/service.php")
    Call<List<Place>> CathcDetail(@Query("location") String location);

    @FormUrlEncoded
    @POST("/insert.php")
    Call<ResponseBody> sendPost (@Field("placeType") String placeType,
                                 @Field("address") String address,
                                 @Field("message") String message);
}
