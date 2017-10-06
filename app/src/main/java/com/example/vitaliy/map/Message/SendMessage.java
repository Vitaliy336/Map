package com.example.vitaliy.map.Message;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.vitaliy.map.R;
import com.example.vitaliy.map.rest.ApiClient;
import com.example.vitaliy.map.rest.ApiInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.vitaliy.map.R.string.Start;


public class SendMessage extends AppCompatActivity {
    Spinner TypeVariants;
    EditText MessageBox, AddressBox;
    Button MessageS, GoTo;
    Context context = this;

    String[] arr1 = {"NextBike", "Parking place", "Rental spot", "Shop and repair station", "Interesting places"};

    private TextWatcher mTextWatcher  = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            checkFieldsForEmptyValues();
        }
    };

    private void checkFieldsForEmptyValues() {
        MessageS = (Button) findViewById(R.id.send);
        if(AddressBox.getText().toString().length() < 10 || MessageBox.getText().toString().length() < 10 ){
            MessageS.setEnabled(false);
        }
        else {
            MessageS.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, arr1);
        TypeVariants = (Spinner)
                findViewById(R.id.PlaceVariants);
        TypeVariants.setAdapter(arrayAdapter);

        AddressBox = (EditText) findViewById(R.id.AddressOption);
        MessageBox = (EditText) findViewById(R.id.MessageEt);
        AddressBox.addTextChangedListener(mTextWatcher);
        MessageBox.addTextChangedListener(mTextWatcher);
        GoTo = (Button)findViewById(R.id.goTo);
        GoTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://laborious-band.000webhostapp.com")));
            }
        });
        checkFieldsForEmptyValues();

        MessageS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {

        String type = TypeVariants.getSelectedItem().toString();
        String addr = AddressBox.getText().toString();
        String mes = MessageBox.getText().toString();


        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = api.sendPost(type, addr, mes);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("OnResponse", "Data Sent");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Message sent")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("On Failure", "Can`t send data");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("OPS... Can`t send your message \n" + "Check your internet connection")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
