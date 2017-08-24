package personal.yulie.android.yuliecamera;

import android.media.Image;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by android on 17-8-19.
 */

public class SaveImgTask implements Runnable {
    public interface Callback {
        void postImageSaved();
    }
    private static final String TAG = SaveImgTask.class.getSimpleName();
    private File mFile;
    private Image mImage;
    private Callback mCallback;

    public SaveImgTask(Image image, File file) {
        mFile = file;
        mImage = image;
    }

    public SaveImgTask(Image image, File file, Callback callback) {
        this(image, file);
        mCallback = callback;
    }
    @Override
    public void run() {
        Log.i(TAG, "run: "+Thread.currentThread());
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try (FileOutputStream output = new FileOutputStream(mFile)){
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
        }
        if (null!=mCallback) {
            mCallback.postImageSaved();
        }
    }
}
