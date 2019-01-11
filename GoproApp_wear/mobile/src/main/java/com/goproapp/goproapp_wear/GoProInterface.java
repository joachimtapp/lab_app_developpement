package com.goproapp.goproapp_wear;


import android.util.Log;

import java.io.IOException;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class GoProInterface {

    private final OkHttpClient client = new OkHttpClient();

    static final String MODE_VIDEO = "VIDEO";
    static final String MODE_PHOTO = "PHOTO";
    static final String MODE_BURST = "BURST";
    static final String ISO_MODE_LOCK = "LOCK";
    static final String ISO_MODE_MAX = "MAX";

    public GoProInterface(){

        // Activate GPS Tag
        sendRequest("http://10.5.5.9/gp/gpControl/setting/83/1");

    }

    public void shutter(){
        sendRequest("http://10.5.5.9/gp/gpControl/command/shutter?p=1");
    }

    public void shutterStop(){
        sendRequest("http://10.5.5.9/gp/gpControl/command/shutter?p=0");
    }

    public void setMode(String mode){
        String url;

        switch (mode){
            case MODE_VIDEO:
                url = "http://10.5.5.9/gp/gpControl/command/sub_mode?mode=0&sub_mode=0";
                break;
            case MODE_PHOTO:
                url = "http://10.5.5.9/gp/gpControl/command/sub_mode?mode=1&sub_mode=1";
                break;
            case MODE_BURST:
                url = "http://10.5.5.9/gp/gpControl/command/sub_mode?mode=2&sub_mode=0";
                break;
            default:
                url = "";
        }

        sendRequest(url);

    }

    public void setFOVPhoto(String fov) {

        String url;

        switch (fov){
            case "Narrow":
                url = "http://10.5.5.9/gp/gpControl/setting/17/9";
                break;
            case "Linear":
                url = "http://10.5.5.9/gp/gpControl/setting/17/10";
                break;
            case "Medium":
                url = "http://10.5.5.9/gp/gpControl/setting/17/8";
                break;
            case "Wide":
                url = "http://10.5.5.9/gp/gpControl/setting/17/0";
                break;
            default:
                url = "";
        }

        sendRequest(url);

    }

    public void setProTune(Boolean enable, String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/";
        switch (mode){
            case MODE_PHOTO:
                url = url + "21/";
                break;
            case MODE_VIDEO:
                url = url + "10/";
                break;
            case MODE_BURST:
                url = url + "34/";
                break;
        }

        if(enable){
            url = url + "1";
        } else {
            url = url + "0";
        }

        sendRequest(url);

    }

    public void setWBAuto(String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/";
        switch (mode){
            case MODE_PHOTO:
                url = url + "22/";
                break;
            case MODE_VIDEO:
                url = url + "11/";
                break;
            case MODE_BURST:
                url = url + "35/";
                break;
        }
        url = url + "0";
        sendRequest(url);
    }

    public void setISOMode(String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/74/";
        switch (mode){
            case ISO_MODE_LOCK:
                url = url + "1";
                break;
            case ISO_MODE_MAX:
                url = url + "0";
                break;
        }
        sendRequest(url);
    }

    public void setISO(Integer iso){
        String url = "http://10.5.5.9/gp/gpControl/setting/13/";
        switch (iso){
            case 0:
                url = url + "2";
                break;
            case 1:
                url = url + "4";
                break;
            case 2:
                url = url + "1";
                break;
            case 3:
                url = url + "3";
                break;
            case 4:
                url = url + "0";
                break;
        }
        sendRequest(url);
    }

    public void setISOMin(Integer iso_min, String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/";

        switch (mode){
            case MODE_PHOTO:
                url = url + "75/";
                break;
            case MODE_BURST:
                url = url + "76/";
                break;
        }
        switch (iso_min){
            case 0:
                url = url + "3";
                break;
            case 1:
                url = url + "2";
                break;
            case 2:
                url = url + "1";
                break;
            case 3:
                url = url + "0";
                break;
            case 4:
                url = url + "4";
                break;
            default:
                url = url + "4";
        }

        sendRequest(url);
    }

    public void setISOMax(Integer iso_max, String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/";

        switch (mode){
            case MODE_PHOTO:
                url = url + "24/";
                break;
            case MODE_BURST:
                url = url + "37/";
                break;
        }
        switch (iso_max){
            case 0:
                url = url + "3";
                break;
            case 1:
                url = url + "2";
                break;
            case 2:
                url = url + "1";
                break;
            case 3:
                url = url + "0";
                break;
            case 4:
                url = url + "4";
                break;
            default:
                url = url + "4";
        }

        sendRequest(url);
    }

    public void setShutterAuto(){
        sendRequest("http://10.5.5.9/gp/gpControl/setting/97/0");
    }

    public void setShutter(Integer shutter){
        String url = "http://10.5.5.9/gp/gpControl/setting/97/";
        shutter = shutter + 1;
        url = url + shutter.toString();
        sendRequest(url);
    }

    public void setResVideo(String resolution){
        String url = "http://10.5.5.9/gp/gpControl/setting/2/";
        switch (resolution){
            case "480p":
                url = url + "17";
                break;
            case "720p":
                url = url + "12";
                break;
            case "960p":
                url = url + "10";
                break;
            case "1080p":
                url = url + "9";
                break;
            case "1440p":
                url = url + "7";
                break;
            case "2.7K 4:3":
                url = url + "6";
                break;
            case "2.7K":
                url = url + "4";
                break;
            case "4K":
                url = url + "1";
                break;
            default:
                url = url + "9";
                break;
        }

        sendRequest(url);
    }

    public void setFPSVideo(String fps){
        String url = "http://10.5.5.9/gp/gpControl/setting/3/";

        switch (fps) {
            case "24 FPS":
                url = url + "9";
                break;
            case "30 FPS":
                url = url + "8";
                break;
            case "48 FPS":
                url = url + "7";
                break;
            case "60 FPS":
                url = url + "5";
                break;
            case "80 FPS":
                url = url + "4";
                break;
            case "90 FPS":
                url = url + "3";
                break;
            case "100 FPS":
                url = url + "2";
                break;
            case "120 FPS":
                url = url + "1";
                break;
            case "240 FPS":
                url = url + "0";
                break;
            default:
                url = url + "5";
                break;
        }

        sendRequest(url);
    }

    public void setEV(Integer ev, String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/";

        switch (mode){
            case MODE_PHOTO:
                url = url + "26/";
                break;
            case MODE_VIDEO:
                url = url + "15/";
                break;
            case MODE_BURST:
                url = url + "39/";
                break;
        }
        ev = 8 - ev;
        url = url + ev.toString();

        sendRequest(url);
    }

    public void setFOVVideo(String fov){
        String url = "http://10.5.5.9/gp/gpControl/setting/4/";
        switch (fov){
            case "Narrow":
                url = url + "2";
                break;
            case "Linear":
                url = url + "4";
                break;
            case "Medium":
                url = url + "1";
                break;
            case "Wide":
                url = url + "0";
                break;
            case "Super View":
                url = url + "3";
                break;
            default:
                url = url + "0";
                break;
        }

        sendRequest(url);

    }

    public void setBurstRate(String burstRate){
        String url = "http://10.5.5.9/gp/gpControl/setting/29/";
        switch (burstRate){
            case "3/1":
                url = url + "0";
                break;
            case "5/1":
                url = url + "1";
                break;
            case "10/1":
                url = url + "2";
                break;
            case "10/2":
                url = url + "3";
                break;
            case "10/3":
                url = url + "4";
                break;
            case "30/1":
                url = url + "5";
                break;
            case "30/2":
                url = url + "6";
                break;
            case "30/3":
                url = url + "7";
                break;
            case "30/6":
                url = url + "8";
                break;
        }

        sendRequest(url);
    }

    public void setFOVBurst(String fov){
        String url;

        switch (fov){
            case "Narrow":
                url = "http://10.5.5.9/gp/gpControl/setting/28/9";
                break;
            case "Linear":
                url = "http://10.5.5.9/gp/gpControl/setting/28/10";
                break;
            case "Medium":
                url = "http://10.5.5.9/gp/gpControl/setting/28/8";
                break;
            case "Wide":
                url = "http://10.5.5.9/gp/gpControl/setting/28/0";
                break;
            default:
                url = "";
        }

        sendRequest(url);
    }

    public void setWB(Integer wb, String mode){
        String url = "http://10.5.5.9/gp/gpControl/setting/";
        switch (mode){
            case MODE_VIDEO:
                url = url + "11/";
                break;
            case MODE_PHOTO:
                url = url + "22/";
                break;
            case MODE_BURST:
                url = url + "35/";
                break;
        }
        switch (wb){
            case 0:
                url = url + "1";
                break;
            case 1:
                url = url + "5";
                break;
            case 2:
                url = url + "6";
                break;
            case 3:
                url = url + "2";
                break;
            case 4:
                url = url + "7";
                break;
            case 5:
                url = url + "3";
                break;
            default:
                url = url + "1";
        }

        sendRequest(url);
    }

    private void sendRequest(String url){
        //HttpGetRequest httpGetRequest = new HttpGetRequest();
        //httpGetRequest.execute(url);

        final Request startpreview = new Request.Builder()
                .url(HttpUrl.get(URI.create(url)))
                .build();

        client.newCall(startpreview).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Log.d("GoPro","Camera not connected");
                }


            }
        });
    }

}