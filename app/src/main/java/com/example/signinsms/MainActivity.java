package com.example.signinsms;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;

import java.util.Hashtable;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText edt_phone, edt_code;
    LinearLayout layout_sendphone, layout_sendcode, layout_success;
    TextView send_phone, send_code, txt_again;
    IntentFilter intentFilter;
    AppSMSBroadcastReceiver appSMSBroadcastReceiver;
    Context context = MainActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initView();
        smsListener();
        initBroadcast();
    }




    private void initView()
     {
            layout_sendphone = findViewById(R.id.layout_sendphone);
            layout_sendcode = findViewById(R.id.layout_sendcode);
            layout_success = findViewById(R.id.layout_success);
            send_phone = findViewById(R.id.txt_sendphone);
            send_code = findViewById(R.id.txt_send_code);
            txt_again = findViewById(R.id.txt_again);
            edt_phone = findViewById(R.id.edt_phone);
            edt_code = findViewById(R.id.edt_code);

            send_phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(edt_phone.getText().length() == 11 && edt_phone.getText().toString().startsWith("09"))
                        sendcodewithsms(edt_phone.getText().toString());
                    else
                        Toast.makeText(context, "شماره تلفن وارد شده درست نیست!" , Toast.LENGTH_LONG ).show();

                }
            });


            // In case the user clicks the confirm (تایید شماره همراه) TextView
            // Otherwise the code will automatically completes the edt_txt with the sent code (see line 230)
            send_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    checkAuth(edt_code.getText().toString(), edt_phone.getText().toString());
                }
            });

            txt_again.setOnClickListener(view ->
            {
                layout_sendphone.setVisibility(View.VISIBLE);
                send_code.setVisibility(View.GONE);
                layout_success.setVisibility(View.GONE);
            });

    }



    private void sendcodewithsms(String mobile)
    {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("لطفاً کمی صبر کنید");
        dialog.setTitle("در حال ارسال کد");
        dialog.show();

        String hashcode = "";
        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);

        for (String signature : appSignatureHelper.getAppSignatures() ) hashcode = signature;

        String finalHashCode = hashcode;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, parameters.LINKSMS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String result) {
                        dialog.dismiss();
                        if (Integer.valueOf(result) > 2000){
                            Toast.makeText(context, "پیامک برای شما ارسال شد", Toast.LENGTH_LONG).show();
                            layout_sendphone.setVisibility(View.GONE);
                            layout_sendcode.setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.txt_show_phone)).setText(mobile);
                        }else {
                            Toast.makeText(context, "خطا در ارسال پیامک", Toast.LENGTH_LONG).show();
                            layout_sendphone.setVisibility(View.GONE);
                            layout_sendcode.setVisibility(View.VISIBLE);

                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(context, "Error!!", Toast.LENGTH_LONG).show();
                        layout_sendphone.setVisibility(View.GONE);
                        layout_sendcode.setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.txt_show_phone)).setText(mobile);


                    }
                })
        {
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new Hashtable<>();
                params.put("UserName", parameters.USERNAME);
                params.put("Password", parameters.PASSWORD);
                params.put("Mobile", mobile);
                params.put("Footer", finalHashCode);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);


    }



    private void smsListener()
    {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsRetriever();
    }

    private void initBroadcast()
    {
        intentFilter = new IntentFilter("com.google.android.gms.auth.api.phone.SMS_RETRIEVED");
        appSMSBroadcastReceiver = new AppSMSBroadcastReceiver();

        appSMSBroadcastReceiver.setOnSmsReceiverListener(new AppSMSBroadcastReceiver.OnSmsReceiverListener(){

            @Override
            public void onReceive(String code)
            {
                String number = code.replaceAll("[^0-9]", "");
                number = number.substring(0, 6);
                edt_code.setText(number.trim());
                checkAuth(edt_code.getText().toString(), edt_phone.getText().toString());
            }
        });

    }

    private void checkAuth(String code, String mobile)
    {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("لطفاً کمی صبر کنید");
        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, parameters.CHECKSMS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String result) {
                        dialog.dismiss();
                        if (Boolean.parseBoolean(result)){
                            Toast.makeText(context, "احراز هویت با موفقیت انجام شد خوش آمدید", Toast.LENGTH_LONG).show();
                            layout_sendcode.setVisibility(View.GONE);
                            layout_success.setVisibility(View.VISIBLE);

                        }else{
                            Toast.makeText(context, "کد وارد شده صحیح نمی باشد", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(context, "Error!!", Toast.LENGTH_LONG).show();
                    }
                })
        {
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new Hashtable<>();
                params.put("UserName", parameters.USERNAME);
                params.put("Password", parameters.PASSWORD);
                params.put("Mobile", mobile);
                params.put("Code", code);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(appSMSBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(appSMSBroadcastReceiver);
    }
}