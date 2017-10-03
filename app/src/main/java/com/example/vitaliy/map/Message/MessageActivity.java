package com.example.vitaliy.map.Message;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.vitaliy.map.R;
import com.example.vitaliy.map.rest.ApiClient;
import com.example.vitaliy.map.rest.ApiInterface;
import com.google.android.gms.common.api.Api;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MessageActivity extends AppCompatActivity {
    ArrayList<String> nextBike;
    ArrayList<String> parking;
    ArrayList<String> rental;
    ArrayList<String> shopAndRepair;
    ArrayList<String> spots;
    Spinner sOptions, variants;
    EditText message;
    Button send;
    private static final String URL = "https://laborious-band.000webhostapp.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        nextBike = getIntent().getStringArrayListExtra("NB");
        parking = getIntent().getStringArrayListExtra("P");
        rental = getIntent().getStringArrayListExtra("R");
        shopAndRepair = getIntent().getStringArrayListExtra("SH");
        spots = getIntent().getStringArrayListExtra("S");

        shopAndRepair = new ArrayList<>(new TreeSet<>(shopAndRepair));
        String[] options = {"NextBike", "Parking place", "Rental spot", "Shop and repair station", "Interesting places"};

        sOptions = (Spinner) findViewById(R.id.options);
        variants = (Spinner) findViewById(R.id.variants);
        message = (EditText) findViewById(R.id.Message);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sOptions.setAdapter(adapter);

        sOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);

                if (item.matches("Parking place")) {
                    createSoms(parking);
                }
                if (item.matches("Rental spot")) {
                    createSoms(rental);
                }
                if (item.matches("NextBike")) {
                    createSoms(nextBike);
                }
                if (item.matches("Shop and repair station")) {
                    createSoms(shopAndRepair);
                }
                if (item.matches("Interesting places")) {
                    createSoms(spots);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        send = (Button) findViewById(R.id.post);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataToServer();
            }
        });

    }

    private void sendDataToServer() {
        String pl = sOptions.getSelectedItem().toString();
        String addr = variants.getSelectedItem().toString();
        String ms = message.getText().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        postApi api = retrofit.create(postApi.class);
        Call<ResponseBody> call = api.ppp(pl, addr, ms);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("Syker", "Sent");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Syker", "fail");
            }
        });
    }

    private void createSoms(ArrayList<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getAddresses(list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        variants.setAdapter(adapter);
    }

    private List<String> getAddresses(ArrayList<String> coords) {
        List<Address> adresses;
        List<String> rAddress = new ArrayList<>();
        Geocoder geocoder;
        StringBuilder strReturnedAddress;
        String ls[];
        Address returnedAddress;
        for (int i = 0; i < coords.size(); i++) {
            ls = coords.get(i).substring(10, coords.get(i).length() - 1).split(",");
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                adresses = geocoder.getFromLocation(Double.parseDouble(ls[0]), Double.parseDouble(ls[1]), 1);
                if (adresses != null) {
                    returnedAddress = adresses.get(0);
                    strReturnedAddress = new StringBuilder("");
                    for (int j = 0; j <= returnedAddress.getMaxAddressLineIndex(); j++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(j)).append("\n");
                    }
                    rAddress.add(strReturnedAddress.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rAddress;
    }
}