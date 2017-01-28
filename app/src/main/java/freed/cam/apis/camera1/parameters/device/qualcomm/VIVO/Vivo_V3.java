package freed.cam.apis.camera1.parameters.device.qualcomm.VIVO;

import android.hardware.Camera;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.parameters.manual.ManualParameterInterface;
import freed.cam.apis.basecamera.parameters.modes.MatrixChooserParameter;
import freed.cam.apis.camera1.parameters.device.BaseQcomDevice;
import freed.dng.DngProfile;

/**
 * Created by troop on 13.10.2016.
 */

public class Vivo_V3 extends BaseQcomDevice {
    public Vivo_V3(Camera.Parameters parameters, CameraWrapperInterface cameraUiWrapper) {
        super(parameters, cameraUiWrapper);
    }

    @Override
    public ManualParameterInterface getExposureTimeParameter() {
        return null;
    }

    @Override
    public ManualParameterInterface getIsoParameter() {
        return null;
    }
}
