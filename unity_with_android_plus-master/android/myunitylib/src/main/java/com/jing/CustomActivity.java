package com.jing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Arrays;

public class CustomActivity extends UnityPlayerActivity {



    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i("unity_with_android_plus", "老子就是自定义的Activity");
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        System.out.println("---------------------- " +  requestCode + "\t" + Arrays.toString(permissions) + "\t" + Arrays.toString(grantResults));

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        String str ="全部授权";
        for (int i=0;i<grantResults.length;i++)
        {
            if (grantResults[i]!=0)
            {
                str=permissions[i]+"未被授权";
                break;
            }
        }



        UnityPlayer.UnitySendMessage("Main Camera","FromAndroid",str);







    }
}
