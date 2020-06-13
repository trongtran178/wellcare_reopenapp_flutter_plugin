package vn.wellcare.callkeep2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

/**
 * Callkeep2Plugin
 */
public class Callkeep2Plugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native
    /// Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine
    /// and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private static Context context;
    private static final String TAG_NOTIFICATION = "WELLCARE_TAG_NOTIFICATION";
    private static final String CHANNEL_ID = "WELLCARE_CHANNEL";
    private static final String TAG = "Receiver";
    private static final int NOTIFICATION_ID = 111111;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "callkeep2");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    // This static function is optional and equivalent to onAttachedToEngine. It
    // supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new
    // Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith
    // to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith
    // will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both
    // be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "callkeep2");
        channel.setMethodCallHandler(new Callkeep2Plugin());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("reopenAppFromBackground")) {
            try {
                if (context != null) {
                    // APP STILL ALIVE
                    final String packageName = call.argument("packageName");
                    // DETECT SCREEN ON OFF - PENDING
                    if (!isScreenOn(context)) {
                        try {
                            PowerManager.WakeLock wakeLock = ((PowerManager) context
                                    .getSystemService(Context.POWER_SERVICE)).newWakeLock(
                                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                            "VN_WELLCARE:VN_WELLCARE_WAKELOCK_TAG");
                            wakeLock.acquire(1 * 60 * 1000L /* 10 minutes */);
                            wakeLock.release();
                        } catch (Exception e) {
                            System.out.println(73 + " - Co loi xay ra - Wakelock - " + e.getMessage());
                        }
                    }
                    // Open app from background
                    if (Build.VERSION.SDK_INT < 29) {
                        // THIS BELOW CODE JUST WORK WITH ANDROID VERSION LOWER THAN 29 ;)
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                        context.startActivity(launchIntent);
                    } else {
                        // for Restrictions on start activity from background - ANDROID VERSION GREATER
                        // THAN OR EQUAL 29
                        // FullScreenIntent here
                        startActivityNotification(context, packageName, NOTIFICATION_ID, "WELLCARE TITLE",
                                "WELLCARE MESSAGE");
                    }
                } else {
                    // APP BEING TERMINATED
                    System.out.println("104 - Context is null");
                }
                result.success("Nhung ngay dep troi");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            result.notImplemented();
        }
    }

    private boolean isScreenOn(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : displayManager.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            System.out.println(screenOn);
            return screenOn;
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            System.out.println(powerManager.isScreenOn());
            return powerManager.isScreenOn();
        }
    }

    // Notification method to support opening activities on Android 10
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void startActivityNotification(Context context, String packageName, int notificationID, String title,
            String message) {

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder;

        Intent contentIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(context).setContentTitle(title).setContentText(message)
                .setAutoCancel(true).setContentIntent(contentPendingIntent).setSmallIcon(R.drawable.launch_background)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setCategory(NotificationCompat.CATEGORY_CALL).setPriority(NotificationCompat.PRIORITY_MAX)
                .setFullScreenIntent(contentPendingIntent, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Activity Opening Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Activity opening notification");

            notificationBuilder.setChannelId(CHANNEL_ID);

            Objects.requireNonNull(mNotificationManager).createNotificationChannel(notificationChannel);
        }

        Objects.requireNonNull(mNotificationManager).notify(TAG_NOTIFICATION, notificationID,
                notificationBuilder.build());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        System.out.println("Callkeep2Plugin - onAttachedToActivity");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        System.out.println("Callkeep2Plugin - onDetachedFromActivityForConfigChanges");

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        System.out.println("Callkeep2Plugin - onReattachedToActivityForConfigChanges");

    }

    @Override
    public void onDetachedFromActivity() {
        System.out.println("Callkeep2Plugin - onDetachedFromActivity");

    }
}
