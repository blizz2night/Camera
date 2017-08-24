package personal.yulie.android.yuliecamera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements UIFragment.Callbacks, CameraFragment.Callbacks{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Fragment cameraFragment = CameraFragment.newInstance();
            transaction.replace(R.id.preview_container,cameraFragment);
            Fragment uiFragment = UIFragment.newInstance();
            transaction.replace(R.id.ui_fragment_container,uiFragment);
            transaction.commit();
        }
    }

    @Override
    public void handleEvent(int request) {
        FragmentManager fm = getSupportFragmentManager();
            CameraFragment fragment = (CameraFragment) fm.findFragmentById(R.id.preview_container);
            fragment.handleEvent(request);
        }

    @Override
    public void changeRecordBtnIcon(boolean isRecording) {
        FragmentManager fm = getSupportFragmentManager();
        UIFragment fragment = (UIFragment) fm.findFragmentById(R.id.ui_fragment_container);
        fragment.changeRecordIcon(isRecording);
    }

    @Override
    public void setButtonIsClickable(int resource, boolean isClickable) {
        FragmentManager fm = getSupportFragmentManager();
        UIFragment fragment = (UIFragment) fm.findFragmentById(R.id.ui_fragment_container);
        fragment.setButtonIsClickable(resource, isClickable);
    }

    @Override
    public void setButtonsIsClickable(boolean isClickable) {
        FragmentManager fm = getSupportFragmentManager();
        UIFragment fragment = (UIFragment) fm.findFragmentById(R.id.ui_fragment_container);
        fragment.setButtonsIsClickable(isClickable);
    }
}
