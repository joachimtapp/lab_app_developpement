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
            case "WVGA":
                indices = new ArrayList<>(Arrays.asList(9));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "720p":
                indices = new ArrayList<>(Arrays.asList(1, 2, 4, 5, 8, 9));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "720p S":
                indices = new ArrayList<>(Arrays.asList(4, 5, 8));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "960p":
                indices = new ArrayList<>(Arrays.asList(4, 5, 8));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "1080p":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 7, 8));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "1080p S":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "1440p":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "2.7K 4:3":
                indices = new ArrayList<>(Arrays.asList(1, 2));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "2.7K S":
                indices = new ArrayList<>(Arrays.asList(1, 2));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "2.7K":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "4K":
                indices = new ArrayList<>(Arrays.asList(0, 1, 2));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
            case "4K S":
                indices = new ArrayList<>(Arrays.asList(0));
                indices.forEach(ind -> output.add(fps.get(ind)));
                break;
        }
        return output;
    }

    public ArrayList<String> getFov(String resolution){

        ArrayList<String> output = new ArrayList<>();
        output.add(fov.get(2));
        switch (resolution) {
            case "720p":
                output.add(fov.get(1));
                output.add(fov.get(0));
                break;
            case "1080p":
                output.add(fov.get(1));
                output.add(fov.get(0));
                break;
            case "2.7K":
                output.add(fov.get(1));
                break;
        }
        return output;
    }
}
