package freed.cam.apis.camera1.parameters.device.mediatek.VERNEE;

import android.hardware.Camera;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.parameters.modes.MatrixChooserParameter;
import freed.cam.apis.camera1.parameters.device.BaseMTKDevice;
import freed.dng.DngProfile;

/**
 * Created by GeorgeKiarie on 11/16/2016.
 */
public class VERNEE_APOLLO_Lite extends BaseMTKDevice {
    public VERNEE_APOLLO_Lite(Camera.Parameters parameters, CameraWrapperInterface cameraUiWrapper) {
        super(parameters, cameraUiWrapper);
    }
}
