package freed.cam.apis.camera2.modules.capture;

import android.graphics.ImageFormat;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import java.io.IOException;

import freed.image.ImageTask;
import freed.net.ClientThread;
import freed.utils.BackgroundHandlerThread;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class AbstractImageCapture implements ImageCaptureInterface {

    private final String TAG = AbstractImageCapture.class.getSimpleName();
    protected final int max_images;
    private final ImageReader imageReader;
    private BackgroundHandlerThread backgroundHandlerThread;
    private boolean setToPreview = false;
    public Image image;
    protected CaptureResult result;
    protected ImageTask task;
    public ClientThread client_thread = null;

    public AbstractImageCapture(Size size, int format, boolean setToPreview, int max_images)
    {
        backgroundHandlerThread = new BackgroundHandlerThread("AbstractImageCapture");
        backgroundHandlerThread.create();
        this.setToPreview = setToPreview;
        this.max_images = max_images;
        imageReader = ImageReader.newInstance(size.getWidth(),size.getHeight(),format,max_images);
        imageReader.setOnImageAvailableListener(this,backgroundHandlerThread.getBackgroundHandler());
    }

    public void resetTask()
    {
        task = null;
    }

    @Override
    public ImageTask getSaveTask() {
        synchronized (this) {
            if (task == null) {
                Log.d(TAG, "Task is null wait");
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return task;
    }

    @Override
    public Surface getSurface()
    {
        return imageReader.getSurface();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.d(TAG,"onImageAvailable");
        synchronized (this)
        {
            image = reader.acquireLatestImage();


            Log.d(TAG, "Got format "+ String.valueOf(image.getFormat()));
            if (client_thread != null && image.getFormat() == ImageFormat.RAW_SENSOR) {
                try {
                    freed.utils.Log.d(TAG, "Sending captured image");
                    client_thread.SendImage(image);
                    freed.utils.Log.d(TAG, "Image sent");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client_thread = null;
            }


            createTask();
            this.notifyAll();
            Log.d(TAG, "Add new img to queue");
        }
    }

    protected  abstract void createTask();

    @Override
    public void setCaptureResult(CaptureResult  captureResult)
    {
        synchronized (this)
        {
            Log.d(TAG,"setCaptureResult");
            result = captureResult;
            createTask();
            this.notifyAll();
        }
    }

    @Override
    public boolean setToPreview() {
        return setToPreview;
    }

    @Override
    public void release()
    {
        synchronized (this)
        {
            this.notifyAll();
        }
        Log.d(TAG,"release");
        if (imageReader != null)
            imageReader.close();
        if (image != null)
            image.close();
        backgroundHandlerThread.destroy();

    }


}
