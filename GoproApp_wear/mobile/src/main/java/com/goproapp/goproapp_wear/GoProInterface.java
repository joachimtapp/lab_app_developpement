package com.goproapp.goproapp_wear;


public class GoProInterface {

    static final String MODE_VIDEO = "VIDEO";
    static final String MODE_PHOTO = "PHOTO";
    static final String MODE_BURST = "BURST";

    public GoProInterface(){

        // Activate GPS Tag
        sendRequest("http://10.5.5.9/gp/gpControl/setting/83/1");

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

    public void setProTunePhoto(Boolean enable){
        String url;

        if(enable){
            url = "http://10.5.5.9/gp/gpControl/setting/21/1";
        } else {
            url = "http://10.5.5.9/gp/gpControl/setting/21/0";
        }

        sendRequest(url);

    }

    public void setBWAutoPhoto(){
        sendRequest("http://10.5.5.9/gp/gpControl/setting/22/0");
    }

    public void setBWPhoto(Integer bw){
        String url = "http://10.5.5.9/gp/gpControl/setting/22/";
        switch (bw){
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

    public void setISOMinPhoto(Integer iso_min){
        String url = "http://10.5.5.9/gp/gpControl/setting/75/";
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

    public void setISOMaxPhoto(Integer iso_max){
        String url = "http://10.5.5.9/gp/gpControl/setting/24/";
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

    public void setShutterAutoPhoto(){
        sendRequest("http://10.5.5.9/gp/gpControl/setting/97/0");
    }

    public void setShutterPhoto(Integer shutter){
        String url = "http://10.5.5.9/gp/gpControl/setting/97/";
        shutter = shutter + 1;
        url = url + shutter.toString();
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

    private void sendRequest(String url){
        HttpGetRequest httpGetRequest = new HttpGetRequest();
        httpGetRequest.execute(url);
    }

}
