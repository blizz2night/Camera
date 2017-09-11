package personal.yulie.android.yuliecamera;

import android.view.TextureView;

/**
 * Created by android on 17-9-11.
 */

public interface IPresenter{
    void handleEvent(int request);

    void changeRecordBtnIcon(boolean isRecording);

    void setButtonIsClickable(int resource, boolean isClickable);

    void setButtonsIsClickable(boolean isClickable);

    void startPreview(TextureView previewView);

    void start();

    void stop() throws InterruptedException;
}
