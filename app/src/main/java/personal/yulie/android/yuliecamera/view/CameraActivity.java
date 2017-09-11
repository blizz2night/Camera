package personal.yulie.android.yuliecamera.view;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import personal.yulie.android.yuliecamera.IPresenter;
import personal.yulie.android.yuliecamera.Presenter;
import personal.yulie.android.yuliecamera.R;

public class CameraActivity extends AppCompatActivity{
    private IPresenter mPresenter;
    private static final int REQUEST_PERM = 0;
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //requestPermissions(PERMISSIONS,REQUEST_PERM);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment cameraFragment = fm.findFragmentById(R.id.preview_container);
        if (null == cameraFragment) {
            cameraFragment = CameraFragment.newInstance();
            transaction.add(R.id.preview_container,cameraFragment);
        }

        Fragment uIFragment = fm.findFragmentById(R.id.ui_fragment_container);

        if (null == uIFragment) {
            uIFragment = UIFragment.newInstance();
            transaction.add(R.id.ui_fragment_container,uIFragment);
        }
        transaction.commit();

        mPresenter = new Presenter(getApplicationContext(), (IView) uIFragment, (IView) cameraFragment);

    }

//    public void handleEvent(int request) {
//        FragmentManager fm = getSupportFragmentManager();
//            CameraFragment fragment = (CameraFragment) fm.findFragmentById(R.id.preview_container);
//            fragment.handleEvent(request);
//        }
//
//    public void changeRecordBtnIcon(boolean isRecording) {
//        FragmentManager fm = getSupportFragmentManager();
//        UIFragment fragment = (UIFragment) fm.findFragmentById(R.id.ui_fragment_container);
//        fragment.changeRecordBtnIcon(isRecording);
//    }
//
//    public void setButtonIsClickable(int resource, boolean isClickable) {
//        FragmentManager fm = getSupportFragmentManager();
//        UIFragment fragment = (UIFragment) fm.findFragmentById(R.id.ui_fragment_container);
//        fragment.setButtonIsClickable(resource, isClickable);
//    }
//
//    public void setButtonsIsClickable(boolean isClickable) {
//        FragmentManager fm = getSupportFragmentManager();
//        UIFragment fragment = (UIFragment) fm.findFragmentById(R.id.ui_fragment_container);
//        fragment.setButtonsIsClickable(isClickable);
//    }


}
