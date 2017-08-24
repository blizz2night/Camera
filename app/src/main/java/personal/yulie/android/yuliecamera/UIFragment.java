package personal.yulie.android.yuliecamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import static personal.yulie.android.yuliecamera.Event.CAPTURE;
import static personal.yulie.android.yuliecamera.Event.RECORD;
import static personal.yulie.android.yuliecamera.Event.SWITCH_CAM;

/**
 * Created by android on 17-8-17.
 */

public class UIFragment extends Fragment{
    private static final String TAG = "UIFragment";

    private ImageButton mSwithButton;
    private ImageButton mGalleryButton;
    private ImageButton mCameraButton;
    private ImageButton mRecordButton;
    private Callbacks mCallbacks;

    public void setBottonIsClickable(final int resource, final boolean isClickable) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                switch (resource) {
                    case R.id.camera_button:
                        mCameraButton.setClickable(isClickable);
                        break;
                    case R.id.switch_button:
                        mSwithButton.setClickable(isClickable);
                        break;
                    case R.id.record_video_button:
                        mRecordButton.setClickable(isClickable);
                        break;
                }
            }
        });

    }

    public void setBottonsIsClickable(final boolean isClickable) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                mCameraButton.setClickable(isClickable);
                mSwithButton.setClickable(isClickable);
                mRecordButton.setClickable(isClickable);
                mGalleryButton.setClickable(isClickable);
            }
        });
    }

    public interface Callbacks {
        void handleEvent(int request);
    }



    public void changeRecordIcon(final boolean isRecording) {
        mRecordButton.post(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    mRecordButton.setImageResource(R.drawable.ic_action_stop);
                } else {
                    mRecordButton.setImageResource(R.drawable.ic_record_video);
                }
            }
        });
    }

    public static UIFragment newInstance() {
        Bundle args = new Bundle();
        UIFragment fragment = new UIFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ui, container, false);
        mSwithButton = (ImageButton) view.findViewById(R.id.switch_button);
        mSwithButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSwithButton.setClickable(false);
                disableButtons();
                mCallbacks.handleEvent(SWITCH_CAM);
            }
        });
        mGalleryButton= (ImageButton) view.findViewById(R.id.gallery_button);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                disableButtons();
//                PackageManager pm = getActivity().getPackageManager();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_GALLERY);
                intent = Intent.createChooser(intent, "Choose~");
//                ResolveInfo info = null;
//                for (ResolveInfo activity : activities) {
//                    Log.i(TAG, activity.loadLabel(pm).toString());
//                    if (activity.loadLabel(pm).toString().equals("PhotoGallery")) {
//                        info = activity;
//                        break;
//                    }
//                }
//
//                intent = new Intent(Intent.ACTION_MAIN).setClassName(
//                        info.activityInfo.applicationInfo.packageName,
//                        info.activityInfo.name
//                );
//                disableButtons();
                startActivity(intent);
//                disableButtons();

            }
        });
        mCameraButton = (ImageButton) view.findViewById(R.id.camera_button);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();

                mCallbacks.handleEvent(CAPTURE);
            }
        });
        mRecordButton = (ImageButton) view.findViewById(R.id.record_video_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();


                mCallbacks.handleEvent(RECORD);
            }
        });
        return view;
    }

    private void disableButtons() {
        mRecordButton.setClickable(false);
        mCameraButton.setClickable(false);
        mSwithButton.setClickable(false);
        mGalleryButton.setClickable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setBottonsIsClickable(true);
    }

}
