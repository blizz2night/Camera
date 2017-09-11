package personal.yulie.android.yuliecamera.view;

import personal.yulie.android.yuliecamera.IPresenter;

/**
 * Created by android on 17-9-11.
 */

public interface IView {
    void setPresenter(IPresenter presenter);

    void changeRecordBtnIcon(boolean isRecording);

    void setButtonIsClickable(int resource, boolean isClickable);

    void setButtonsIsClickable(boolean isClickable);
}
