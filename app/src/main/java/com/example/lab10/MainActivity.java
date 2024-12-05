package com.example.lab10;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.widget.MediaController;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private AudioPlayerService audioPlayerService;
    private boolean isBound = false;
    private MediaController mediaController;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.AudioPlayerBinder binder = (AudioPlayerService.AudioPlayerBinder) service;
            audioPlayerService = binder.getService();
            isBound = true;
            Log.d("MainActivity", "Service conectado");

            // Configurar el MediaController
            mediaController.setMediaPlayer(MainActivity.this);
            mediaController.setAnchorView(findViewById(R.id.mediaControllerContainer));
            mediaController.setEnabled(true);

            // Mostrar el MediaController al conectar el servicio
            mediaController.show();
            Log.d("MainActivity", "MediaController configurado y mostrado");

            // Registrar el observador de ciclo de vida
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver(audioPlayerService));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            audioPlayerService = null;
            Log.d("MainActivity", "Service desconectado");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el MediaController
        mediaController = new MediaController(this);

        // Registrar el lanzador para solicitar permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permiso concedido, puedes iniciar el servicio
                        startAndBindService();
                    } else {
                        // Permiso denegado, manejar según corresponda
                        Log.d("MainActivity", "Permiso de notificación denegado");
                    }
                }
        );

        // Verificar si el permiso ya está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Solicitar el permiso
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // Permiso ya concedido
            startAndBindService();
        }
    }

    private void startAndBindService() {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        ContextCompat.startForegroundService(this, serviceIntent); // Usar startForegroundService
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        Log.d("MainActivity", "Servicio iniciado y vinculado");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vincular el servicio si no está ya vinculado
        if (!isBound) {
            Intent serviceIntent = new Intent(this, AudioPlayerService.class);
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
            Log.d("MainActivity", "Intentando vincular al servicio en onStart");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Desvincular el servicio
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
            Log.d("MainActivity", "Servicio desvinculado en onStop");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Mostrar controles al tocar la pantalla
        mediaController.show();
        return super.onTouchEvent(event);
    }

    // Métodos de MediaPlayerControl
    @Override
    public void start() {
        if (isBound && audioPlayerService != null) {
            Log.d("MainActivity", "MediaController: start() llamado");
            audioPlayerService.play();
        }
    }

    @Override
    public void pause() {
        if (isBound && audioPlayerService != null) {
            Log.d("MainActivity", "MediaController: pause() llamado");
            audioPlayerService.pause();
        }
    }

    @Override
    public int getDuration() {
        if (isBound && audioPlayerService != null) {
            return audioPlayerService.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (isBound && audioPlayerService != null) {
            return audioPlayerService.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (isBound && audioPlayerService != null) {
            audioPlayerService.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        if (isBound && audioPlayerService != null) {
            return audioPlayerService.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        if (isBound && audioPlayerService != null) {
            return audioPlayerService.getAudioSessionId();
        }
        return 0;
    }
}
