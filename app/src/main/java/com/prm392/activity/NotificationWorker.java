package com.prm392.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.prm392.R;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String certificateName = getInputData().getString("certificate_name");
        int daysLeft = getInputData().getInt("days_left", 0);

        if (certificateName != null) {
            showNotification(certificateName, daysLeft);
        }
        return Result.success();
    }

    private void showNotification(String certificateName, int daysLeft) {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel (cho Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "certificate_reminder",
                    "Certificate Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), "certificate_reminder")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Chứng chỉ sắp hết hạn")
                .setContentText(certificateName + " sẽ hết hạn trong " + daysLeft + " ngày")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}