package personal.yulie.android.yuliecamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Created by android on 17-8-17.
 */

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener, ImageReader.OnImageAvailableListener {
    public static final String TAG = "CameraFragment";
    private static final int REQUEST_CAMERA_PERMISSION = 0;

    private TextureView mPreviewView;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    //private ImageReader mImageReader;
    private android.util.Size mPreviewSize;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private CameraCaptureSession mCaptureSession;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader mImageReader;

    public static CameraFragment newInstance() {
        Bundle args = new Bundle();
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }
    

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: has cam" + checkCameraHardware(getActivity()));
        Log.i(TAG, "onResume: "+mPreviewView.isAvailable());
//        mHandlerThread = new HandlerThread("Camera2");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void onPause() {
        closeCamera();
//        stopBackgroundThread();
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
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics ch = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                    return;
                }
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    private void createCameraPreviewSession() {
        Log.i(TAG, "createCameraPreviewSession: ");
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
//        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
//                ImageFormat.JPEG, 6);
//        mImageReader.setOnImageAvailableListener(this,mHandler);
        mPreviewBuilder.addTarget(surface);
        //???
        //mPreviewBuilder.addTarget(mImageReader.getSurface());
        try {
            mCameraDevice.createCaptureSession(
                    //Arrays.asList(surface, mImageReader.getSurface()),
                    Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (null == mCameraDevice) {
                                return;
                            }
                            mCaptureSession = session;
                            try {
                                mCaptureSession.setRepeatingRequest(
                                        mPreviewBuilder.build(),
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
        } catch (CameraAccessException e) {
            e.printStackTrace();
            //mImageReader.close();
            //mCameraDevice.close();
            //mHandlerThread.quit();
        }
    }



    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onImageAvailable(ImageReader reader) {
//        Image img = reader.acquireNextImage();
//        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
//        byte[] date = new byte[buffer.remaining()];
//        buffer.get(data);
//        img.close();

    }


    private void stopBackgroundThread() {
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mHandlerThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


}
