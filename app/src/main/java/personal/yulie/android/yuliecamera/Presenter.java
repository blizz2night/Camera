package personal.yulie.android.yuliecamera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.TextureView;

import personal.yulie.android.yuliecamera.view.IView;

import static personal.yulie.android.yuliecamera.utils.Event.CAPTURE;
import static personal.yulie.android.yuliecamera.utils.Event.RECORD;
import static personal.yulie.android.yuliecamera.utils.Event.SWITCH_CAM;

/**
 * Created by android on 17-9-11.
 */

public class Presenter implements IPresenter, CameraContext.Callbacks{
    private Context mContext;
    private IView mUIView;
    private IView mCameraView;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private CameraContext mCameraContext;

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    public static final String TAG = "Presenter";

    @Override
    @MainThread
    public void handleEvent(int request) {
        switch (request) {
            case CAPTURE:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "handleEvent: CAPTURE " + Thread.currentThread());
//                        capture();
                        mCameraContext.capture();
                        sMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "run: SaveImg");
                                mUIView.setButtonsIsClickable(true);
                            }
                        });
                    }
                });
                break;
            case RECORD:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(!mCameraContext.isRecording()){
                                mCameraContext.startRecord();
                                sMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mUIView.changeRecordBtnIcon(true);
                                        mUIView.setButtonIsClickable(R.id.record_video_button, true);
                                    }
                                });
                            } else {
                                Log.i(TAG, "handleEvent: Stop RECORD" + Thread.currentThread());
                                mCameraContext.stopRecord();
                                sMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
//                                        showToast("Save record");
                                        mUIView.changeRecordBtnIcon(false);
                                        mUIView.setButtonsIsClickable(true);
                                    }
                                });
                            }
                        } catch (CameraAccessException e) {
                            Log.e(TAG, "run: record", e);
                        }
                    }
                });
                break;
            case SWITCH_CAM:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "handleEvent: SWITCH_CAM" + Thread.currentThread());
                        try {
                            mCameraContext.switchCamera();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        sMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mUIView.setButtonsIsClickable(true);
                            }
                        });
                    }
                });
                break;
        }
    }
    @Override
    public void changeRecordBtnIcon(boolean isRecording) {

    }

    @Override
    public void setButtonIsClickable(int resource, boolean isClickable) {

    }

    @Override
    public void setButtonsIsClickable(boolean isClickable) {
        sMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: SaveImg");
                mUIView.setButtonsIsClickable(true);
            }
        });
    }

    public Presenter(Context context, IView uIView, IView cameraView) {
        mContext = context;
        mUIView = uIView;
        mCameraView = cameraView;
        mUIView.setPresenter(this);
        mCameraView.setPresenter(this);
    }

    private void startBackgroundThread() {
        Log.d(TAG, "startBackgroundThread: ");
        mHandlerThread = new HandlerThread("Camera2");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    private void stopBackgroundThread() throws InterruptedException {
        Log.d(TAG, "stopBackgroundThread: ");
        if (null != mHandlerThread && mHandlerThread.isAlive()) {
            mHandlerThread.quitSafely();
            mHandlerThread.join();
            mHandlerThread = null;
            mHandler = null;

        }
    }

    public void startPreview(TextureView previewView) {
        mCameraContext.setPreviewView(previewView);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onSurfaceTextureAvailable: InitCam" + Thread.currentThread());

                try {
                    mCameraContext.setupCamera();
                    mCameraContext.openCamera();
                    sMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mUIView.setButtonsIsClickable(true);
                        }
                    });

                } catch (CameraAccessException e) {
                    Log.e(TAG, "SurfaceTexture init cam run: " + Thread.currentThread(), e);
                }
            }
        });
    }

    @Override
    public void start() {
        mCameraContext = new CameraContext(mContext);
        mCameraContext.setCallbacks(this);
        startBackgroundThread();
    }

    @Override
    public void stop(){
        if (mCameraContext.isRecording()) {
        mCameraContext.setRecording(false);
        mUIView.changeRecordBtnIcon(false);
        }
        mCameraContext.closeSession();
        mCameraContext.closeCamera();
        mCameraContext.setCallbacks(null);
        mCameraContext = null;
        //mContext = null;
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postImageAvailable() {
        sMainHandler.post(new Runnable() {
            @Override
            public void run() {
                setButtonsIsClickable(true);
            }
        });
    }
}
