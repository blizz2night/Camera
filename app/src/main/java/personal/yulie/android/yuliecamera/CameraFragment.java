package personal.yulie.android.yuliecamera;

import android.Manifest;
import android.app.Activity;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

import static personal.yulie.android.yuliecamera.Event.CAPTURE;
import static personal.yulie.android.yuliecamera.Event.RECORD;
import static personal.yulie.android.yuliecamera.Event.SWITCH_CAM;

/**
 * Created by android on 17-8-17.
 */

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener,
        ImageReader.OnImageAvailableListener{
    public static final String TAG = "PreviewFragment";
    private static final int MESSAGE_SAVE_IMAGE = 0;

    private TextureView mPreviewView;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private Size mPreviewSize;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private CameraCaptureSession mPreviewSession;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private ImageReader mImageReader;
    private File mSaveDir;
    private String[] mCameraIds;
    private Size mRecordSize;
    private MediaRecorder mMediaRecorder;
    private CameraCaptureSession mRecordSession;
    private CaptureRequest.Builder mRecordRequestBuilder;
    private boolean mIsRecording = false;
    private String mRecordOutputUrl;
    private Callbacks mCallbacks;
    private File mSaveImgFile;

    public interface Callbacks {
        void changeRecordBtnIcon(boolean isRecording);

        void setButtonIsClickable(int resource, boolean isClickable);

        void setButtonsIsClickable(boolean isClickable);
    }

    public static CameraFragment newInstance() {
        Bundle args = new Bundle();
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        mPreviewView = (TextureView) view.findViewById(R.id.preview_container);
        mPreviewView.setSurfaceTextureListener(this);
        mSaveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "YulieCam");
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
//        Log.i(TAG, "onResume: has cam" + checkCameraHardware(getActivity()));
//        Log.i(TAG, "onResume: "+mPreviewView.isAvailable());
        startBackgroundThread();
        if (mPreviewView.isAvailable()) {
            try {
                // TODO: 17-8-23

                if (!checkCameraPermission()) {
                    return;
                }
                setupCamera(mCameraId, mPreviewView.getWidth(), mPreviewView.getHeight());
                mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void startBackgroundThread() {
        Log.d(TAG, "startBackgroundThread: ");
        mHandlerThread = new HandlerThread("Camera2");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        Log.d(TAG, "stopBackgroundThread: ");
        if (null != mHandlerThread && mHandlerThread.isAlive()) {
            mHandlerThread.quitSafely();
            try {
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable: Init");

        try {
            if (!checkCameraPermission()) return;
            Log.i(TAG, "onSurfaceTextureAvailable: "+Thread.currentThread());
            setupCamera(width, height);
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        try {
            mPreviewSize = new Size(width, height);
            setupCamera(mCameraId, height, width);
            startPreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private boolean checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                return false;
            }
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        return true;
    }

    private void setupCamera(String cameraId, int width, int height) throws CameraAccessException {
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        mCameraIds = mCameraManager.getCameraIdList();
        mCameraId = cameraId;
        CameraCharacteristics ch = mCameraManager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width,
                height);
        Size imageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), width, height);
        mImageReader = ImageReader.newInstance(
                imageSize.getWidth(),
                imageSize.getHeight(),
                ImageFormat.JPEG, 6
        );
        mMediaRecorder = new MediaRecorder();
//        mRecordSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), width, height);
        mRecordSize = new Size(1280, 720);
        mImageReader.setOnImageAvailableListener(this, mHandler);
    }

    private void setupCamera(int width, int height) throws CameraAccessException {
        setupCamera("0", width, height);
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
            Log.i(TAG, "onOpened: "+Thread.currentThread());
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

            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    private void startPreview() throws CameraAccessException {
        Log.i(TAG, "startPreview: "+Thread.currentThread());
        SurfaceTexture surfaceTexture = mPreviewView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(surfaceTexture);
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
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

    public void handleEvent(int request) {
        try {
            switch (request) {
                case CAPTURE:
                    Log.i(TAG, "handleEvent: CAPTURE " + Thread.currentThread());
                    //mCallbacks.setButtonIsClickable(R.id.camera_button,true);
                    capture();
                    break;
                case RECORD:
                    if (!mIsRecording) {
                        mIsRecording = true;
                        Log.i(TAG, "handleEvent: Start RECORD " + Thread.currentThread());
                        startRecord();
                        mMediaRecorder.start();
                        mCallbacks.changeRecordBtnIcon(mIsRecording);
                        mCallbacks.setButtonIsClickable(R.id.record_video_button,true);
                    } else {
                        Log.i(TAG, "handleEvent: Stop RECORD" + Thread.currentThread());
                        startPreview();
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        mIsRecording = false;
                        mCallbacks.changeRecordBtnIcon(mIsRecording);
                        Toast.makeText(getActivity(), "Save to" + mRecordOutputUrl, Toast.LENGTH_SHORT).show();
                        mCallbacks.setButtonsIsClickable(true);
                    }
                    break;
                case SWITCH_CAM:
                    Log.i(TAG, "handleEvent: SWITCH_CAM" + Thread.currentThread());
                    closeCamera();
                    mCameraId = String.valueOf((Integer.parseInt(mCameraId) + 1) % mCameraIds.length);
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    setupCamera(mCameraId, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
                    mCallbacks.setButtonsIsClickable(true);

                    break;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "handleEvent: " + request, e);
        } catch (RuntimeException e) {
            Log.e(TAG, "Stop MediaRecorder Failed",e);
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

        CameraCaptureSession.CaptureCallback mCaptureCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//                Toast.makeText(activity, "captured", Toast.LENGTH_SHORT).show();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("Save to "+mSaveImgFile);
                    //mCallbacks.setButtonIsClickable(R.id.camera_button,true);
                }
            });

        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };
    private void capture(){
        final Activity activity = getActivity();
        if (null == mCameraDevice || null == mImageReader || null == activity) {
            return;
        }

        CaptureRequest.Builder requestBuilder = null;
        try {
            requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        requestBuilder.addTarget(mImageReader.getSurface());
        requestBuilder.build();

        try {
            if (!mIsRecording) {
                mPreviewSession.capture(requestBuilder.build(), mCaptureCaptureCallback, null);
            } else {
                mRecordSession.capture(requestBuilder.build(), mCaptureCaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() throws CameraAccessException {
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
    }

    SaveImgTask.Callback saveImgTaskCallback = new SaveImgTask.Callback() {
        @Override
        public void postImageSaved() {
            mCallbacks.setButtonsIsClickable(true);
        }
    };

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.i(TAG, "onImageAvailable: "+Thread.currentThread());
        mSaveImgFile = getSaveImgFile();
        if (!mSaveImgFile.exists()) {
            SaveImgTask saveImgTask = new SaveImgTask(
                    reader.acquireLatestImage(),
                    mSaveImgFile,
                    saveImgTaskCallback
            );
            mHandler.post(saveImgTask);

        }
    }

    @NonNull
    private File getSaveImgFile() {
        if (!mSaveDir.exists()) {
            if (mSaveDir.mkdir()) {
                Toast.makeText(getActivity(), "mkdir"+mSaveDir, Toast.LENGTH_SHORT).show();
            }
        }
//        Log.i(TAG, "onImageAvailable: "+mSaveDir.toString());
        return new File(mSaveDir,System.currentTimeMillis()+".jpg");
    }

    private void closeCamera() {

            if (null != mPreviewSession) {
                mPreviewSession.close();
                mPreviewSession = null;
            }
            if (null != mRecordSession) {
                mRecordSession.close();
                mRecordSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }

    }
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
