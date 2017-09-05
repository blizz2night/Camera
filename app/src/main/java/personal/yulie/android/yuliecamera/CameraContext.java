package personal.yulie.android.yuliecamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CameraContext implements ImageReader.OnImageAvailableListener {

    public static final String TAG = "CameraContext";
    private static final int REQUEST_PERM = 0;
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Context mContext;
    private TextureView mPreviewView;
    private Size mPreviewSize;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId = "0";
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private ImageReader mImageReader;
    private File mSaveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "YulieCam");
    private String[] mCameraIds;
    private Size mRecordSize;
    private MediaRecorder mMediaRecorder;
    private CameraCaptureSession mRecordSession;
    private CaptureRequest.Builder mRecordRequestBuilder;
    private boolean mIsRecording = false;
    private String mRecordOutputUrl;
    private CameraFragment.Callbacks mCallbacks;
    private File mSaveImgFile;

    public TextureView getPreviewView() {
        return mPreviewView;
    }

    public void setPreviewView(TextureView previewView) {
        mPreviewView = previewView;
    }

    public CameraFragment.Callbacks getCallbacks() {
        return mCallbacks;
    }

    public void setCallbacks(CameraFragment.Callbacks callbacks) {
        mCallbacks = callbacks;
    }


    public boolean isRecording() {
        return mIsRecording;
    }

    public void setRecording(boolean recording) {
        mIsRecording = recording;
    }

    public CameraContext(Context context) {
        mContext = context;
    }

    public void setupCamera() throws CameraAccessException {
        setupCamera(mCameraId, mPreviewView.getWidth(), mPreviewView.getHeight());
    }

    private void setupCamera(String cameraId, int width, int height) throws CameraAccessException {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mCameraIds = mCameraManager.getCameraIdList();
        mCameraId = cameraId;
        CameraCharacteristics ch = mCameraManager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width,
                height);
//        Size imageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), width, height);
        Size[] Sizes = map.getOutputSizes(ImageFormat.JPEG);
        Size imageSize = Sizes[0];
        mImageReader = ImageReader.newInstance(
                imageSize.getWidth(),
                imageSize.getHeight(),
                ImageFormat.JPEG, 1
        );
//        mMediaRecorder = new MediaRecorder();
//        mRecordSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), width, height);
        mRecordSize = new Size(1280, 720);
        mImageReader.setOnImageAvailableListener(this, null);
    }

    private void setupCamera(int width, int height) throws CameraAccessException {
        setupCamera("0", width, height);
    }

    public void openCamera() throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
    }

    public void switchCamera() throws CameraAccessException {
        closeCamera();
        mCameraId = String.valueOf((Integer.parseInt(mCameraId) + 1) % mCameraIds.length);
        setupCamera(mCameraId, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) (lhs.getWidth() * lhs.getHeight()) -
                    (long) (rhs.getWidth() * rhs.getHeight()));
        }
    }

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecordOutputUrl = new StringBuilder(mSaveDir.getAbsolutePath())
                .append(File.separator)
                .append(System.currentTimeMillis())
                .append(".mp4").toString();
        mMediaRecorder.setOutputFile(mRecordOutputUrl);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mRecordSize.getWidth(), mRecordSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "onOpened: " + Thread.currentThread());
            mCameraDevice = camera;
            try {
                startPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            mContext = null;
        }
    };

    private void startPreview() throws CameraAccessException {
        Log.i(TAG, "startPreview: "+Thread.currentThread());
        SurfaceTexture surfaceTexture = mPreviewView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(surfaceTexture);
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        initCaptureRequest();
        mPreviewRequestBuilder.addTarget(surface);
        mCameraDevice.createCaptureSession(
                Arrays.asList(surface, mImageReader.getSurface()),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (null == mCameraDevice) {
                            return;
                        }
                        mPreviewSession = session;
                        try {
                            mPreviewSession.setRepeatingRequest(
                                    mPreviewRequestBuilder.build(),
                                    null,
                                    null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                },
                null
        );

    }

    CameraCaptureSession.CaptureCallback mCaptureCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.i(TAG, "onCaptureCompleted: ");
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };
    public void capture(){
        if (null == mCameraDevice || null == mImageReader || null == mContext) {
            return;
        }
        try {
            if (!mIsRecording) {
                mPreviewSession.capture(mCaptureRequest, mCaptureCaptureCallback, null);
            } else {
                mRecordSession.capture(mCaptureRequest, mCaptureCaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initCaptureRequest() {
        CaptureRequest.Builder requestBuilder = null;
        try {
            requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        requestBuilder.addTarget(mImageReader.getSurface());
        mCaptureRequest =requestBuilder.build();
    }

    public void startRecord() throws CameraAccessException {
        mIsRecording = true;
        try {
            setupMediaRecorder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SurfaceTexture surfaceTexture = mPreviewView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        mRecordRequestBuilder =  mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        mRecordRequestBuilder.addTarget(previewSurface);
        Surface recordSurface = mMediaRecorder.getSurface();
        mRecordRequestBuilder.addTarget(recordSurface);
        mCameraDevice.createCaptureSession(
                Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        mRecordSession = session;
                        try {
                            mRecordSession.setRepeatingRequest(mRecordRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                },
                null
        );
        mMediaRecorder.start();
    }

    public void stopRecord() throws CameraAccessException {
        startPreview();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mIsRecording = false;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.i(TAG, "onImageAvailable: "+Thread.currentThread());
        mSaveImgFile = getSaveImgFile();
        if (!mSaveImgFile.exists()) {
            new SaveImgTask(reader.acquireLatestImage(),
                    mSaveImgFile).executeOnExecutor(SaveImgTask.THREAD_POOL_EXECUTOR);
        }
    }

    @NonNull
    private File getSaveImgFile() {
        if (!mSaveDir.exists()) {
            if (mSaveDir.mkdir()) {
                //showToast("mkdir"+mSaveDir);
                Log.i(TAG, "getSaveImgFile: "+mSaveDir.toString());
            }
        }
//        Log.i(TAG, "onImageAvailable: "+mSaveDir.toString());
        return new File(mSaveDir,System.currentTimeMillis()+".jpg");
    }

    public void closeCamera() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void closeSession() {
        if (null != mPreviewSession) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
        if (null != mRecordSession) {
            mRecordSession.close();
            mRecordSession = null;
        }
    }


}
