package freed.cam.featuredetector;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.troop.freedcam.BuildConfig;
import com.troop.freedcam.R;

import freed.image.ImageManager;
import freed.image.ImageTask;
import freed.utils.AppSettingsManager;
import freed.utils.Log;

/**
 * Created by troop on 16.07.2017.
 */

public class CameraFeatureDetectorFragment extends Fragment {

    public interface FeatureDetectorEvents
    {
        void featuredetectorDone();
    }

    private TextView loggerview;
    private AppSettingsManager appSettingsManager;
    private FeatureDetectorEvents featureDetectorEvents;
    private final String TAG = CameraFeatureDetectorFragment.class.getSimpleName();
    private CameraFeatureRunner featureRunner;
    private FdUiHandler handler = new FdUiHandler();

    public void setAppSettingsManagerAndListner(AppSettingsManager appSettingsManager, FeatureDetectorEvents events)
    {
        this.appSettingsManager = appSettingsManager;
        this.featureDetectorEvents = events;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.camerafeaturedetector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loggerview = (TextView)view.findViewById(R.id.textview_log);
        loggerview.setMovementMethod(new ScrollingMovementMethod());

    }

    @Override
    public void onResume() {
        super.onResume();
        if (featureRunner == null){
            featureRunner = new CameraFeatureRunner();
            ImageManager.putImageLoadTask(featureRunner);
        }
    }

    private void sendLog(String log)
    {
        String tmp = log + " \n"+ loggerview.getText().toString();
        loggerview.setText(tmp);
    }

    private void startFreedcam()
    {
        featureRunner = null;
        appSettingsManager.setAppVersion(BuildConfig.VERSION_CODE);
        appSettingsManager.setAreFeaturesDetected(true);
        featureDetectorEvents.featuredetectorDone();
        Log.d(TAG,"startFreeDcam");
    }



    private AbstractFeatureDetectorTask.ProgressUpdate cameraListner = new AbstractFeatureDetectorTask.ProgressUpdate() {
        @Override
        public void onProgessUpdate(String msg) {
            Log.d(TAG, msg);
            handler.obtainMessage(MSG_SENDLOG, msg).sendToTarget();

        }

        @Override
        public void onTaskEnd(String msg) {
        }
    };

    private class CameraFeatureRunner extends ImageTask
    {
        @Override
        public boolean process() {
            if (Build.VERSION.SDK_INT >= 21) {
                new Camera2FeatureDetectorTask(cameraListner,appSettingsManager,getContext()).detect();
            }
            new Camera1FeatureDetectorTask(cameraListner, appSettingsManager).detect();
            if (appSettingsManager.hasCamera2Features())
                appSettingsManager.setCamApi(AppSettingsManager.API_2);
            handler.obtainMessage(MSG_STARTFREEDCAM).sendToTarget();
            return false;
        }
    }


    private final int MSG_STARTFREEDCAM = 0;
    private final int MSG_SENDLOG = 1;

    private class FdUiHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STARTFREEDCAM:
                    startFreedcam();
                    break;
                case MSG_SENDLOG:
                    sendLog((String)msg.obj);
                    break;
                default:
                super.handleMessage(msg);
            }
        }
    }
}
