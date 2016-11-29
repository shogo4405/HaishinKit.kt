package com.haishinkit.view;

import android.content.Context;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.hardware.Camera;

import java.io.IOException;

public class CameraView extends SurfaceView {
    private Camera camera = null;

    private final SurfaceHolder.Callback surfaceCallback = new Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (camera == null) {
                return;
            }
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (camera != null) {
                camera.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera == null) {
                return;
            }
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    };

    public CameraView(final Context context) {
        super(context);
        final SurfaceHolder holder = getHolder();
        camera = Camera.open();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(surfaceCallback);
    }

    public Camera getCamera() {
        return camera;
    }
}
