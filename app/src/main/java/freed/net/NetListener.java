package freed.net;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.utils.Log;


public class NetListener extends Thread {
    private static final String TAG = "NetLister";
    private CameraWrapperInterface cameraUiWrapper;

    public NetListener(CameraWrapperInterface wrapper){
        this.cameraUiWrapper = wrapper;
    }

    @Override
    public void run() {
        ServerSocket socket=null;
        try {
            Log.d(this.TAG, "Starting listening");
            try {
                socket = new ServerSocket(6789);
            } catch (IOException e) {
                Log.e(this.TAG, "Error on socket bind");
                e.printStackTrace();
            }
            while (true) {
                try {
                    Socket clientSocket = socket.accept();
                    new ClientThread(clientSocket, cameraUiWrapper).start();
                } catch (IOException e) {
                    Log.e(this.TAG, "Error on connection accept!");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

