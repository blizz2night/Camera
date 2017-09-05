package personal.yulie.android.yuliecamera;

import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by android on 17-8-19.
 */

public class SaveImgTask extends AsyncTask<Void, Void, Void> {
    public static final int BUFFER_SIZE = 4096 * 4;
    @Override
    protected Void doInBackground(Void... params) {
        Log.i(TAG, "run: "+Thread.currentThread());
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try (
                InputStream in = new ByteArrayInputStream(bytes);
                FileOutputStream output = new FileOutputStream(mFile);
                BufferedOutputStream out = new BufferedOutputStream(output,BUFFER_SIZE);
        ){
            int len;
            byte[] byteBuffer = new byte[BUFFER_SIZE];
            while ((len = in.read(byteBuffer))>0) {
                out.write(byteBuffer,0,len);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
        }
        if (null!=mCallback) {
            mCallback.postImageSaved();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

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

}
