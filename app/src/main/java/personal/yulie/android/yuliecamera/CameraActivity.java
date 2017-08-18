package personal.yulie.android.yuliecamera;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        FragmentManager fm = getSupportFragmentManager();
        if (null == savedInstanceState) {
            FragmentTransaction transaction = fm.beginTransaction();
            CameraFragment cameraFragment = CameraFragment.newInstance();
            transaction.replace(R.id.preview_container,cameraFragment);
            UIFragment uiFragment = UIFragment.newInstance();
            transaction.replace(R.id.ui_fragment_container,uiFragment);
            transaction.commit();
        }



    }


}
