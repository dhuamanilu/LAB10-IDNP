package com.example.lab10;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

public class AudioPlayerService extends Service {
    public static final String CHANNEL_ID = "AudioPlayerServiceChannel";
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new AudioPlayerBinder();
    private boolean isNotificationVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio); // Asegúrate de tener el archivo en res/raw
        mediaPlayer.setLooping(false);

        mediaPlayer.setOnCompletionListener(mp -> {
            stopSelf();
            Log.d("AudioPlayerService", "Reproducción completada, servicio detenido");
        });

        Log.d("AudioPlayerService", "Service creado");
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d("AudioPlayerService", "Reproducción iniciada");
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d("AudioPlayerService", "Reproducción pausada");
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("AudioPlayerService", "Reproducción detenida");
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AudioPlayerService", "onStartCommand llamado");

        // Solo mostrar la notificación si la aplicación está en segundo plano
        if (!isAppInForeground()) {
            startForegroundServiceNotification();
        }

        return START_STICKY;
    }

    private void startForegroundServiceNotification() {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reproducción en curso")
                .setContentText("El audio se está reproduciendo.")
                .setSmallIcon(R.drawable.ic_music_note) // Asegúrate de tener este ícono en res/drawable
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
        isNotificationVisible = true;
        Log.d("AudioPlayerService", "Notificación de foreground iniciada");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Player Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("AudioPlayerService", "Canal de notificación creado");
            }
        }
    }

    public void showNotification() {
        if (isNotificationVisible) return;

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reproducción en curso")
                .setContentText("El audio se está reproduciendo.")
                .setSmallIcon(R.drawable.ic_music_note)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
        isNotificationVisible = true;
        Log.d("AudioPlayerService", "Notificación mostrada");
    }

    public void hideNotification() {
        if (!isNotificationVisible) return;

        stopForeground(true);
        isNotificationVisible = false;
        Log.d("AudioPlayerService", "Notificación ocultada");
    }

    private boolean isAppInForeground() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("AudioPlayerService", "onBind llamado");
        return binder;
    }

    public class AudioPlayerBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("AudioPlayerService", "Servicio destruido y MediaPlayer liberado");
        }
    }
}
