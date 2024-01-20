package com.example.signinsms;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;


public class AppSignatureHelper extends ContextWrapper {

    public static final String TAG = AppSignatureHelper.class.getSimpleName();
    private static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;

    public AppSignatureHelper(Context base) {
        super(base);
    }

    public ArrayList<String> getAppSignatures()
    {
        ArrayList<String> appCodes = new ArrayList<>();

        try{
            String packageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            Signature[] signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;

            for (Signature signature: signatures)
            {
                String hash = hash(packageName, signature.toCharsString());
                if(hash != null)
                    appCodes.add(String.format("%s", hash));

            }
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appCodes;
    }

    private static String hash(String packageName, String signature)
    {
         String appInfo = packageName + " " + signature;
         try{
             MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
             messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
             byte[] hashSignature = messageDigest.digest();

             hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
             String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
             base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);
             return base64Hash;

         }catch(NoSuchAlgorithmException e) {e.printStackTrace();}

         return null;
    }

}
