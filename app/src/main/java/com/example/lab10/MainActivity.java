package com.example.lab10;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.widget.MediaController;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private AudioPlayerService audioPlayerService;
    private boolean isBound = false;
    private MediaController mediaController;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.AudioPlayerBinder binder = (AudioPlayerService.AudioPlayerBinder) service;
            audioPlayerService = binder.getService();
            isBound = true;

            // Configurar el MediaController
            mediaController.setMediaPlayer(MainActivity.this);
            mediaController.setAnchorView(findViewById(R.id.mediaControllerContainer));
            mediaController.setEnabled(true);

            // Mostrar el MediaController al conectar el servicio
            mediaController.show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaController = new MediaController(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
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
        audioPlayerService.play();
    }

    @Override
    public void pause() {
        audioPlayerService.pause();
    }

    @Override
    public int getDuration() {
        return audioPlayerService.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return audioPlayerService.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        audioPlayerService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return audioPlayerService.isPlaying();
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
        return audioPlayerService.getAudioSessionId();
    }
}
