package personal.yulie.android.yuliecamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by android on 17-8-17.
 */

public class UIFragment extends Fragment {
    public static final String TAG = "UI";
    ImageButton mSwithButton;
    ImageButton mGalleryButton;
    ImageButton mCameraButton;
    ImageButton mRecordButton;

    public static UIFragment newInstance() {
        Bundle args = new Bundle();
        UIFragment fragment = new UIFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ui, container, false);
        mSwithButton = (ImageButton) view.findViewById(R.id.switch_button);
        mGalleryButton= (ImageButton) view.findViewById(R.id.gallery_button);
        mCameraButton = (ImageButton) view.findViewById(R.id.camera_button);
        mRecordButton = (ImageButton) view.findViewById(R.id.record_video_button);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
