package com.goproapp.goproapp_wear;

import java.util.ArrayList;
import java.util.Arrays;

public class GoProCombinations {

    private ArrayList<String> fps;
    private ArrayList<String> fov;

    public GoProCombinations(String[] mfps, String[] mfov){

        fps = new ArrayList<>(Arrays.asList(mfps));
        fov = new ArrayList<>(Arrays.asList(mfov));

    }

    public ArrayList<String> getFPS(String resolution){

        ArrayList<Integer> indices;
        ArrayList<String> output = new ArrayList<>();
        switch (resolution){
            case "480p":
                indices = new ArrayList<>(Arrays.asList(8));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "720p":
                indices = new ArrayList<>(Arrays.asList(1, 3, 6, 7, 8));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "960p":
                indices = new ArrayList<>(Arrays.asList(3, 7));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "1080p":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 7));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "1440p":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "2.7K 4:3":
                indices = new ArrayList<>(Arrays.asList(1));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "2.7K":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "4K":
                indices = new ArrayList<>(Arrays.asList(0, 1));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
        }
        return output;
    }

    public ArrayList<String> getFov(String resolution, String mFps){

        ArrayList<String> output = new ArrayList<>();
        ArrayList<Integer> indices;
        switch (resolution){
            case "480p":
                indices = new ArrayList<>(Arrays.asList(3));
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "720p":
                switch (mFps){
                    case "30 FPS":
                        indices = new ArrayList<>(Arrays.asList(0, 2, 3));
                        break;
                    case "60 FPS":
                        indices = new ArrayList<>(Arrays.asList(0, 2, 3, 4));
                        break;
                    case "100 FPS":
                        indices = new ArrayList<>(Arrays.asList(4));
                        break;
                    case "120 FPS":
                        indices = new ArrayList<>(Arrays.asList(0, 2, 3, 4));
                        break;
                    default:
                        indices = new ArrayList<>(Arrays.asList(1));
                }
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "960p":
                indices = new ArrayList<>(Arrays.asList(3));
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "1080p":
                switch (mFps){
                    case "80 FPS":
                        indices = new ArrayList<>(Arrays.asList(4));
                        break;
                    case "90 FPS":
                        indices = new ArrayList<>(Arrays.asList(3));
                        break;
                    case "120 FPS":
                        indices = new ArrayList<>(Arrays.asList(0, 3));
                        break;
                    default:
                        indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
                        break;
                }
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "1440p":
                indices = new ArrayList<>(Arrays.asList(3));
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "2.7K 4:3":
                indices = new ArrayList<>(Arrays.asList(3));
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "2.7K":
                switch (mFps){
                    case "30 FPS":
                        indices = new ArrayList<>(Arrays.asList(1, 2, 3));
                        break;
                    default:
                        indices = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
                        break;
                }
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
            case "4K":
                switch (mFps){
                    case "24 FPS":
                        indices = new ArrayList<>(Arrays.asList(3, 4));
                        break;
                    default:
                        indices = new ArrayList<>(Arrays.asList(3));
                        break;
                }
                indices.forEach(ind -> output.add(fov.get(ind)));
                break;
        }
        return output;
    }
}
