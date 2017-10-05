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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SendMessage extends AppCompatActivity {
    Spinner TypeVariants, addressVariants;
    EditText MessageBox;
    Button MessageS;
    List<String> nextBike, parking, rental, shopArepair, spots;

    String[] arr1 = {"NextBike", "Parking place", "Rental spot", "Shop and repair station", "Interesting places"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

         nextBike = getIntent().getStringArrayListExtra("NB");
         parking = getIntent().getStringArrayListExtra("P");
         rental = getIntent().getStringArrayListExtra("R");
         shopArepair = getIntent().getStringArrayListExtra("SH");
         spots = getIntent().getStringArrayListExtra("S");

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        TypeVariants = (Spinner) findViewById(R.id.PlaceVariants);
        addressVariants = (Spinner) findViewById(R.id.AddressOptions);
        MessageBox = (EditText) findViewById(R.id.MessageEt);
        TypeVariants.setAdapter(adapter);

        TypeVariants.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                switch (item) {
                    case "Parking place":
                        initAddressVariants((ArrayList<String>) parking);
                        break;
                    case "Rental spot":
                        initAddressVariants((ArrayList<String>) rental);
                        break;
                    case "NextBike":
                        initAddressVariants((ArrayList<String>) nextBike);
                        break;
                    case "Shop and repair station":
                        initAddressVariants((ArrayList<String>) shopArepair);
                        break;
                    case "Interesting places":
                        initAddressVariants((ArrayList<String>) spots);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MessageS = (Button) findViewById(R.id.send);
        MessageS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void initAddressVariants(ArrayList<String> arList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addressVariants.setAdapter(adapter);
    }

    private void sendMessage() {
        String type = TypeVariants.getSelectedItem().toString();
        String addr = addressVariants.getSelectedItem().toString();
        String mes = MessageBox.getText().toString();

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = api.sendPost(type, addr, mes);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("OnResponse", "Data Sent");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("On Failure", "Can`t send data");
            }
        });
    }

    private List<String> getAddresses(ArrayList<String> coords) {
        List<Address> adresses;
        List<String> rAddress = new ArrayList<>();
        Geocoder geocoder;
        String ls[];
        for (int i = 0; i < coords.size(); i++) {
            ls = coords.get(i).substring(10, coords.get(i).length() - 1).split(",");
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                adresses = geocoder.getFromLocation(Double.parseDouble(ls[0]), Double.parseDouble(ls[1]), 1);
                String adr = adresses.get(0).getAddressLine(0);
                rAddress.add(adr.replaceAll("\'", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rAddress;
    }
}
