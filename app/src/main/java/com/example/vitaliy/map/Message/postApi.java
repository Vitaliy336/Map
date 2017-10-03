package com.example.vitaliy.map.Message;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Vitaliy on 10/3/2017.
 */

public interface postApi {
    @FormUrlEncoded
    @POST("/insert.php")
    Call<ResponseBody> ppp(
            @Field("placeType") String placeType,
            @Field("address") String address,
            @Field("message") String message);
}
