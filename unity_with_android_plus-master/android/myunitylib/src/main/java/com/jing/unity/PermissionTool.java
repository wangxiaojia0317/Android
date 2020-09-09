package com.jing.unity;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;


/**
 * https://blog.csdn.net/scimence/article/details/85989997
 * permissionTool.java: 安卓权限请求 <uses-sdk android:targetSdkVersion="23" />
 *
 * 用法1：
 * 1、请求权限 			PermissionTool.Request(activity);
 * 2、处理权限请求结果 		PermissionTool.onRequestPermissionsResult(activity, requestCode, permissions, grantResults);
 * 3、系统设置权限执行回调 	PermissionTool.onActivityResult(this, requestCode, resultCode, data);
 *
 * 简易用法：继承PermissionActivity
 *
 * ----- 2019-1-7 上午9:08:39 scimence */
public class PermissionTool
{
    /** 请求权限 */
    public static void Request(Activity activity)
    {
        String[] permissions = getPermissions(activity);	// 获取应用的所有权限
        requestPermissionProcess(activity, permissions);	// 执行权限请求逻辑
    }

    /** 获取AndroidManifest.xml中所有permission信息， 返回信息如{"android.permission.INTERNET", "android.permission.READ_PHONE_STATE"} */
    public static String[] getPermissions(Activity activity)
    {
        String[] permissions = new String[] {};
        try
        {
            PackageManager packageManager = activity.getPackageManager();
            String packageName = activity.getPackageName();

            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            permissions = packageInfo.requestedPermissions;
        }
        catch (Exception e)
        {

        }
        return permissions;
    }


    /** 请求所需权限 如： String[] permissions = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE }; */
    public static void requestPermissionProcess(final Activity activity, final String... permissions)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {

                // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                int sdkVersion = activity.getApplicationInfo().targetSdkVersion;
                if (Build.VERSION.SDK_INT >= 23 && sdkVersion >= 23)
                {
                    // 检查该权限是否已经获取
                    ArrayList<String> list = new ArrayList<String>();
                    for (String permission : permissions)
                    {
                        try
                        {
                            // int ret = ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission);
                            int ret = activity.checkPermission(permission, Process.myPid(), Process.myUid());

                            // 权限是否已经 授权 GRANTED---授权 DINIED---拒绝
                            if (ret != PackageManager.PERMISSION_GRANTED && !list.contains(permission)) list.add(permission);
                        }
                        catch (Exception ex)
                        {
                            Log.e("permissionTool", "是否已授权,无法判断权限：" + permission);
                        }
                    }

                    // 请求没有的权限
                    if (list.size() > 0)
                    {
                        String[] permission = list.toArray(new String[list.size()]);
                        activity.requestPermissions(permission, PermissionRquestCode);	// 从权限请求返回
                    }
                    else
                    {
                        Log.e("permissionTool", "应用所需权限，均已授权。" );
                        CallBak();
                    }

                }
                else
                {
                    CallBak();
                }
            }
        });
    }

    /** Android 6.0以上版本需要请求的权限信息(targetSdkVision >= 23) */
    private static String[] SettingPermission = new String[] { "android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_SMS",
            "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECEIVE_MMS", "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS",
            "android.permission.GET_ACCOUNTS", "android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE", "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG", "android.permission.ADD_VOICEMAIL", "android.permission.USE_SIP", "android.permission.PROCESS_OUTGOING_CALLS",
            "android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR", "android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BODY_SENSORS", "android.permission.RECORD_AUDIO" };
    private static List<String> permissinList = Arrays.asList(SettingPermission);


    final static int PermissionRquestCode = 6554;

    /** 处理权限请求结果逻辑，再次调用请求、或提示跳转设置界面 */
    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == PermissionRquestCode)
        {
            ArrayList<String> needPermissions = new ArrayList<String>();	// 应用未授权的权限
            ArrayList<String> noaskPermissions = new ArrayList<String>();	// 用户默认拒绝的权限

            for (int i = 0; i < permissions.length; i++)
            {
                String permission = permissions[i];
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    try
                    {
                        // 用户点了默认拒绝权限申请，这时候就得打开自定义dialog，让用户去设置里面开启权限
                        if (!activity.shouldShowRequestPermissionRationale(permission))
                        {
                            Log.i("permissionTool", "permissinList Size：" + permissinList.size());
                            if (permissinList.contains(permission))
                            {
                                noaskPermissions.add(permission);
                            }
                            else
                            {
                                Log.i("permissionTool", "自动允许或拒绝权限：" + permission);
                            }
                        }
                        else
                        {
                            // 记录需要请求的权限信息
                            needPermissions.add(permission);
                        }
                    }
                    catch (Exception ex)
                    {
                        Log.e("permissionTool", "自动允许或拒绝权限,无法判断权限：" + permission);
                    }
                }
            }

            if (needPermissions.size() > 0)
            {
                requestPermissionProcess(activity, needPermissions.toArray(new String[needPermissions.size()]));	// 请求未授予的权限
            }
            else if (noaskPermissions.size() > 0)
            {
                PermissionSetting(activity, noaskPermissions.get(0));	// 对话框提示跳转设置界面，添加权限
            }
            else
            {
                CallBak();
            }
        }
    }

    /** 在手机设置中打开的应用权限 */
    private static void PermissionSetting(final Activity activity, final String permission)
    {
        if (permission.trim().equals("")) return;

        // 获取权限对应的标题和详细说明信息
        String permissionLabel = "";
        String permissionDescription = "";

        try
        {
            PackageManager packageManager = activity.getPackageManager();
            // Tools.showText("permission -> " + permission);

            PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);

            // PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
            // Tools.showText("permission组 -> " + permissionGroupInfo.loadLabel(packageManager).toString());

            permissionLabel = permissionInfo.loadLabel(packageManager).toString();
            // Tools.showText("permission名称 -> " + permissionLabel);

            permissionDescription = permissionInfo.loadDescription(packageManager).toString();
            // Tools.showText("permission描述 -> " + permissionDescription);

        }
        catch (Exception ex)
        {
            return;
        }

        // 自定义Dialog弹窗，显示权限请求
        permissionLabel = "应用需要权限：" + permissionLabel + "\r\n" + permission;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(permissionLabel);
        builder.setMessage(permissionDescription);
        builder.setPositiveButton("去添加 权限", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                // 打开应用对应的权限设置界面
                String action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
                Intent intent = new Intent(action);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, PermissionResultCode);	// 从应用设置界面返回时执行OnActivityResult
            }
        });
        builder.setNegativeButton("拒绝则 退出", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                // 若拒绝了所需的权限请求，则退出应用
                activity.finish();
                System.exit(0);
            }
        });
        builder.show();
    }

    final static int PermissionResultCode = 6555;

    /** Activity执行结果，回调函数 */
    public static void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data)
    {
        // Toast.makeText(activity, "onActivityResult设置权限！", Toast.LENGTH_SHORT).show();
        if (requestCode == PermissionResultCode)	// 从应用权限设置界面返回
        {
            // Toast.makeText(activity, "onActivityResult -> " + resultCode, Toast.LENGTH_SHORT).show();
            PermissionTool.Request(activity);		// 再次进行权限请求（若存在未获取到的权限，则会自动申请）
        }
    }

    // private static boolean isCallBack = false;
    /** 执行权限请求回调逻辑 */
    private static void CallBak()
    {
        // 	if(!isCallBack)
        // 	{
        // 		isCallBack = true;
        if (CallInstance != null) CallInstance.Success();
        // 	}
    }

    // ----------

    /** 权限请求回调 */
    public static abstract class PermissionCallBack
    {
        /** 权限请求成功 */
        public abstract void Success();
    }

    private static PermissionCallBack CallInstance = null;

    /** 请求权限, 请求成功后执行回调逻辑 */
    public static void Request(Activity activity, PermissionCallBack Call)
    {
        CallInstance = Call;
        String[] permissions = getPermissions(activity);	// 获取应用的所有权限

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                CallBak();
            }
        }, 30 * 1000);	// 30秒后自动执行回调逻辑。确保回调会被调用。

        requestPermissionProcess(activity, permissions);	// 执行权限请求逻辑

    }
}