package com.goproapp.goproapp_wear;

import android.os.AsyncTask;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by konrad on 3/14/18.
 */

public class utils {

    static class sendAsyncMagicPacket extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                String sendMessage = "GPHD:0:0:2:0.000000\n";
                byte[] sendData = sendMessage.getBytes();
                InetAddress IPAddress = InetAddress.getByName("10.5.5.9");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8554);
                socket.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

}
