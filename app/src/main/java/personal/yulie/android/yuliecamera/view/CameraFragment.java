package personal.yulie.android.yuliecamera.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import personal.yulie.android.yuliecamera.IPresenter;
import personal.yulie.android.yuliecamera.R;

/**
 * Created by android on 17-8-17.
 */

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener, IView{
    public static final String TAG = "PreviewFragment";
//    private static final int REQUEST_PERM = 0;
//    private static final String[] PERMISSIONS = {
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };

    private TextureView mPreviewView;
//    private Handler mHandler;
//    private HandlerThread mHandlerThread;
//    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
//    private Callbacks mCallbacks;
//    private CameraContext mCameraContext;
    private IPresenter mPresenter;

    @Override
    public void setPresenter(IPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void changeRecordBtnIcon(boolean isRecording) {

    }

    @Override
    public void setButtonIsClickable(int resource, boolean isClickable) {

    }

    @Override
    public void setButtonsIsClickable(boolean isClickable) {

    }

//    public interface Callbacks {
//        void changeRecordBtnIcon(boolean isRecording);
//
//        void setButtonIsClickable(int resource, boolean isClickable);
//
//        void setButtonsIsClickable(boolean isClickable);
//    }

    public static CameraFragment newInstance() {
        Bundle args = new Bundle();
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        mCallbacks = (Callbacks) context;
//        mCameraContext.setCallbacks((Callbacks) context);
    }

    @Override
    public void onDetach() {
//        mCallbacks = null;
//        mCameraContext.setCallbacks(null);
        super.onDetach();
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
        //mCameraContext = new CameraContext(getActivity());
        //mCameraContext.setPreviewView(mPreviewView);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        if (mPreviewView.isAvailable()) {
            mPresenter.startPreview(mPreviewView);
        }
        //startBackgroundThread();
//        if (mPreviewView.isAvailable()) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        checkCameraPermission(PERMISSIONS);
//                        mCameraContext.setupCamera();
//                        mCameraContext.openCamera();
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                    sMainHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mCallbacks.setButtonsIsClickable(true);
//                        }
//                    });
//                }
//            });
//        }
    }

//    private void startBackgroundThread() {
//        Log.d(TAG, "startBackgroundThread: ");
//        mHandlerThread = new HandlerThread("Camera2");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());
//    }
//
//    private void stopBackgroundThread() throws InterruptedException {
//        Log.d(TAG, "stopBackgroundThread: ");
//        if (null != mHandlerThread && mHandlerThread.isAlive()) {
//            mHandlerThread.quitSafely();
//            mHandlerThread.join();
//            mHandlerThread = null;
//            mHandler = null;
//
//        }
//    }

    @Override
    public void onPause() {
        try {
            mPresenter.stop();
            //mPresenter = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        if (mCameraContext.isRecording()) {
//            mCameraContext.setRecording(false);
//            mCallbacks.changeRecordBtnIcon(false);
//        }
//        mCameraContext.closeSession();
//        mCameraContext.closeCamera();
//        try {
//            stopBackgroundThread();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        super.onPause();
    }

//    private boolean checkCameraHardware(Context context) {
//        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width, final int height) {
        Log.i(TAG, "onSurfaceTextureAvailable: Init");
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "onSurfaceTextureAvailable: InitCam" + Thread.currentThread());
//
//                try {
//                    if (checkCameraPermission(PERMISSIONS)) {
//                        mCameraContext.setupCamera();
//                        mCameraContext.openCamera();
//                        sMainHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                mCallbacks.setButtonsIsClickable(true);
//                            }
//                        });
//                    } else {
//                        requestPermissions(PERMISSIONS, REQUEST_PERM);
//                    }
//                } catch (CameraAccessException e) {
//                    Log.e(TAG, "SurfaceTexture init cam run: "+Thread.currentThread(), e);
//                }
//            }
//        });

        mPresenter.startPreview(mPreviewView);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        try {
//            mPreviewSize = new Size(width, height);
//            setupCamera(mCameraId, width, height);
//            startPreview();
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private boolean checkCameraPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: no permission");
                getActivity().finish();
                return;
            }
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
//            try {
//                Log.i(TAG, "onRequestPermissionsResult: "+Thread.currentThread());
//                /mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
        }
    }


//
//    @MainThread
//    public void handleEvent(int request) {
//        switch (request) {
//            case CAPTURE:
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i(TAG, "handleEvent: CAPTURE " + Thread.currentThread());
////                        capture();
//                        mCameraContext.capture();
//                        sMainHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast("Save img ");
//                                mCallbacks.setButtonsIsClickable(true);
//                            }
//                        });
//                    }
//                });
//                break;
//            case RECORD:
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            if(!mCameraContext.isRecording()){
//                                mCameraContext.startRecord();
//                                sMainHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        mCallbacks.changeRecordBtnIcon(true);
//                                        mCallbacks.setButtonIsClickable(R.id.record_video_button, true);
//                                    }
//                                });
//                            } else {
//                                Log.i(TAG, "handleEvent: Stop RECORD" + Thread.currentThread());
//                                mCameraContext.stopRecord();
//                                sMainHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showToast("Save record");
//                                        mCallbacks.changeRecordBtnIcon(false);
//                                        mCallbacks.setButtonsIsClickable(true);
//                                    }
//                                });
//                            }
//                        } catch (CameraAccessException e) {
//                            Log.e(TAG, "run: record", e);
//                        }
//                    }
//                });
//                break;
//            case SWITCH_CAM:
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i(TAG, "handleEvent: SWITCH_CAM" + Thread.currentThread());
//                        try {
//                            mCameraContext.switchCamera();
//                        } catch (CameraAccessException e) {
//                            e.printStackTrace();
//                        }
//                        sMainHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                mCallbacks.setButtonsIsClickable(true);
//                            }
//                        });
//                    }
//                });
//                break;
//        }
//    }

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
