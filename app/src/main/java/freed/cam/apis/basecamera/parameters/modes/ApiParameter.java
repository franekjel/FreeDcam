/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.cam.apis.basecamera.parameters.modes;

import android.os.Build.VERSION;
import android.text.TextUtils;

import javax.inject.Inject;

import freed.cam.apis.CameraFragmentManager;
import freed.cam.apis.basecamera.parameters.AbstractParameter;
import freed.cam.events.EventBusHelper;
import freed.cam.events.SwichCameraFragmentEvent;
import freed.settings.SettingsManager;

/**
 * Created by troop on 21.07.2015.
 */
public class ApiParameter extends AbstractParameter
{

    private SettingsManager settingsManager;
    private CameraFragmentManager cameraFragmentManager;
    @Inject
    public ApiParameter(SettingsManager settingsManager,CameraFragmentManager cameraFragmentManager) {
        super(null);
        this.settingsManager = settingsManager;
        this.cameraFragmentManager = cameraFragmentManager;
        fireStringValueChanged(GetStringValue());
    }

    @Override
    public String[] getStringValues()
    {
        if (VERSION.SDK_INT >= 21)
        {
            if (settingsManager.hasCamera2Features())
                return new String[]{SettingsManager.API_SONY, SettingsManager.API_2, SettingsManager.API_1};
            else
                return new String[]{SettingsManager.API_SONY, SettingsManager.API_1};
        } else
            return new String[]{SettingsManager.API_SONY, SettingsManager.API_1};
    }

    @Override
    public String GetStringValue() {
        String ret = settingsManager.getCamApi();
        if (TextUtils.isEmpty(ret))
            ret = SettingsManager.API_1;
        return ret;
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCamera) {
        settingsManager.setCamApi(valueToSet);
        cameraFragmentManager.unloadCameraFragment();
        cameraFragmentManager.switchCameraFragment();
        fireStringValueChanged(valueToSet);
    }

    @Override
    public ViewState getViewState() {
        return ViewState.Visible;
    }
}
