package personal.yulie.android.yuliecamera;

import android.view.View;

/**
 * Created by android on 17-8-24.
 */

public abstract class NoDoubleClickListener implements View.OnClickListener {
    private final static long DELAY = 1000;
    private long last_time = 0;
    @Override
    public void onClick(View v) {
        long current_time = System.currentTimeMillis();
        if (current_time - last_time >= DELAY) {
            last_time = current_time;
            onNoDoubleClickListener(v);
        }
    }

    protected abstract void onNoDoubleClickListener(View v);
}
