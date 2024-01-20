package com.example.signinsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class AppSMSBroadcastReceiver extends BroadcastReceiver
{
    private OnSmsReceiverListener  onSmsReceiverListener;

    public void setOnSmsReceiverListener(OnSmsReceiverListener onSmsReceiverListener)
    {
        this.onSmsReceiverListener = onSmsReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction()))
        {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

             switch (status.getStatusCode())
            {
                case CommonStatusCodes.SUCCESS:
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    onSmsReceiverListener.onReceive(message);
                    break ;

                case CommonStatusCodes.TIMEOUT:
                    break;
                    
            }

        }

    }

    public interface OnSmsReceiverListener
    {
        void onReceive(String code);
    }

}

