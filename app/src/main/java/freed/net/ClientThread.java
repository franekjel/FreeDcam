package freed.net;

import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.camera2.modules.PictureModuleApi2;
import freed.cam.apis.camera2.parameters.ParameterHandlerApi2;
import freed.utils.Log;

public class ClientThread extends Thread {
    private static final String TAG = "ClientThread";
    private Socket socket;
    private InputStream istream = null;
    private BufferedOutputStream ostream = null;
    private CameraWrapperInterface cameraUiWrapper;

    public ClientThread(Socket socket, CameraWrapperInterface cameraUiWrapper) {
        this.socket = socket;
        this.cameraUiWrapper = cameraUiWrapper;
    }


    public void run() {

        try {
            istream = socket.getInputStream();
            ostream = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.e(this.TAG, "Cannot create reader or writer. Aborting connection");
            e.printStackTrace();
            return;
        }
        try {
            while (true) {
                String msg = ReceiveRequest();
                if (msg.equals("take_photo")){
                    Log.i(this.TAG, "Got take_photo");
                    cameraUiWrapper.getModuleHandler().startWork();
                    Log.d(this.TAG, cameraUiWrapper.getModuleHandler().getCurrentModuleName());

                    PictureModuleApi2 module= (PictureModuleApi2) cameraUiWrapper.getModuleHandler().getCurrentModule();
                    module.client_thread=this;

                    Log.d(this.TAG, "Photo ready");

                } else if (msg.equals("check_connection")){
                    String s="camera_ready";
                    Log.i(this.TAG, "Sending camera_ready " + String.valueOf(s.getBytes().length));
                    Send(s.getBytes(), s.getBytes().length);
                } else if (msg.equals("get_distortion")){
                    ParameterHandlerApi2 parameters = (ParameterHandlerApi2) cameraUiWrapper.getParameterHandler();
                    CameraCharacteristics characteristics =  parameters.cameraHolder.characteristics;
                    float[] calib = null;
                    float[] dist = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        calib = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);
                        dist = characteristics.get(CameraCharacteristics.LENS_DISTORTION);
                    }
                    ByteBuffer i = ByteBuffer.allocate(4);

                    if (calib==null || dist == null){
                        i.putInt(0, 0);
                        Send(i.array(), 4);
                    }else {
                        i.putFloat(0, 36);
                        Send(i.array(), 4);

                        i.putFloat(0, calib[0]);
                        Send(i.array(), 4);
                        i.putFloat(0, calib[1]);
                        Send(i.array(), 4);
                        i.putFloat(0, calib[2]);
                        Send(i.array(), 4);
                        i.putFloat(0, calib[3]);
                        Send(i.array(), 4);

                        i.putFloat(0, dist[0]);
                        Send(i.array(), 4);
                        i.putFloat(0, dist[1]);
                        Send(i.array(), 4);
                        i.putFloat(0, dist[2]);
                        Send(i.array(), 4);
                        i.putFloat(0, dist[3]);
                        Send(i.array(), 4);
                        i.putFloat(0, dist[4]);
                        Send(i.array(), 4);
                    }

                } else{
                    Log.e(this.TAG, "Wrong request: " + msg);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Receive(byte[] buff, int size) throws IOException {
        int totalRead = 0;
        while (totalRead < size){
            int read = istream.read(buff, totalRead, size-totalRead);
            if (read==-1){
                throw new IOException("Error receiving data");
            }
            totalRead += read;
        }
    }

    private void Send(byte[] buff, int size) throws IOException {
        ostream.write(buff, 0, size);
        ostream.flush();
    }

    private String ReceiveRequest() throws IOException {
        byte[] sizeBuff = new byte[4];
        Receive(sizeBuff, 4);
        ByteBuffer bb = ByteBuffer.wrap(sizeBuff);
        bb.order(ByteOrder.BIG_ENDIAN);
        int size = bb.getInt();
        byte[] buff = new byte[size];
        Receive(buff, size);
        return new String(buff);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void SendImage(Image image) throws IOException {
        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer bb = plane.getBuffer();
        int image_size = bb.remaining();
        Log.d(TAG,"Size: " + String.valueOf(image_size/1024)+"KB, resolution: "+String.valueOf(image.getWidth())+"x"+String.valueOf(image.getHeight()));
        Log.d(TAG,"row_stride: " + String.valueOf(plane.getRowStride()));
        Log.d(TAG,"pixel_stride: " + String.valueOf(plane.getPixelStride()));

        ByteBuffer i = ByteBuffer.allocate(4);
        i.putInt(0,image_size+4+4);//size of image + width + height
        Send(i.array(), 4);
        i.putInt(0, plane.getRowStride());
        Send(i.array(), 4);
        i.putInt(0, image.getHeight());
        Send(i.array(), 4);

        byte[] bytes = new byte[image_size];
        bb.get(bytes, 0, image_size);
        Send(bytes, image_size);
    }

}
