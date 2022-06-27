package com.example.dipanshkhandelwal.chess;

import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GestureRecognition {

    private static List<Integer> tipIds = new ArrayList<>(Arrays.asList(4, 8, 12, 16, 20));

    public static List<ArrayList<Integer>> findPosition(HandsResult result, Integer handNo){
        List<ArrayList<Integer>> landmarkList= new ArrayList<>();
        if(!result.multiHandLandmarks().isEmpty()) {
            LandmarkProto.NormalizedLandmarkList myHand = result.multiHandLandmarks().get(handNo);

            int width = 768;
            int height = 1024;

            for (int i = 0; i < myHand.getLandmarkList().size(); i++) {

                int cx = (int) (myHand.getLandmarkList().get(i).getX() * width);
                int cy = (int) (myHand.getLandmarkList().get(i).getY() * height);

                landmarkList.add(new ArrayList<>(Arrays.asList(i, cx, cy)));
            }
        }
        return landmarkList;

    }

    public static Integer getGestureCode(ArrayList<Integer> fingers, HandsResult result){

        if(result.multiHandLandmarks().isEmpty()){
            return null;
        }
        if (fingers.equals(Arrays.asList(0, 0, 0, 0, 0))){
            return 0;
        }else{
            if(fingers.equals(Arrays.asList(0, 1, 0, 0, 0))){
                return 1;
            }else{
                if(fingers.equals(Arrays.asList(0, 1, 1, 0, 0))){
                    return 2;
                }else{
                    if(fingers.equals(Arrays.asList(1, 1, 1, 0, 0))){
                        return 3;
                    }else{
                        if(fingers.equals(Arrays.asList(0, 1, 1, 1, 1))){
                            return 4;
                        }else{
                            if(fingers.equals(Arrays.asList(1, 1, 1, 1, 1))){
                                return 5;
                            }else{
                                if(fingers.equals(Arrays.asList(1, 1, 0, 0, 1))){
                                    return 6;
                                }else{
                                    if(fingers.equals(Arrays.asList(1, 0, 0, 0, 1))){
                                        return 7;
                                    }else{
                                        if(fingers.equals(Arrays.asList(1, 0, 1, 1, 1))){
                                            return 8;
                                        } else {
                                            return null;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static String getHandOrientation(List<ArrayList<Integer>> lmList) {
        String handOrientation;
        if (!lmList.isEmpty()) {
            // Check if the x value of 5th landmark is less than the 17th landmark in order to
            // recognize the hand orientation
            if (lmList.get(5).get(1) < lmList.get(17).get(1)) {
                return handOrientation = "FRONT";
            } else {
                return handOrientation = "BACK";
            }
        }
        else {
            return handOrientation = "UNKNOWN";
        }
    }

    public static ArrayList<Integer> fingersUp(List<ArrayList<Integer>> lmList, String handOrientation){
        if(!lmList.isEmpty()){
            ArrayList<Integer> fingers = new ArrayList<>();
            if (handOrientation.equals("FRONT")){

                // thumb
                if (lmList.get(tipIds.get(0)).get(1) < lmList.get(tipIds.get(0) - 1).get(1)) {
                    fingers.add(1);
                } else {
                    fingers.add(0);
                }

            } else {

                // thumb
                if (lmList.get(tipIds.get(0)).get(1) > lmList.get(tipIds.get(0) - 1).get(1)) {
                    fingers.add(1);
                } else {
                    fingers.add(0);
                }

            }
            // fingers
            for (int i = 1; i <= 4; i++) {
                if (lmList.get(tipIds.get(i)).get(2) < lmList.get(tipIds.get(i) - 1).get(2)) {
                    fingers.add(1);
                } else {
                    fingers.add(0);
                }
            }
            return fingers;

        } else {
            return null;
        }
    }

    public static void logWristLandmark(HandsResult result, boolean showPixelValues, String TAG) {
        // If no landmarks are detected
        if (result.multiHandLandmarks().isEmpty()) {
            return;
        }

        LandmarkProto.NormalizedLandmark wristLandmark =
                result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);
        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {
            int width = result.inputBitmap().getWidth();
            int height = result.inputBitmap().getHeight();
            Log.i(
                    TAG,
                    String.format(
                            "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                            wristLandmark.getX() * width, wristLandmark.getY() * height));
        } else {
            Log.i(
                    TAG,
                    String.format(
                            "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                            wristLandmark.getX(), wristLandmark.getY()));
        }
        if (result.multiHandWorldLandmarks().isEmpty()) {
            return;
        }
        LandmarkProto.Landmark wristWorldLandmark =
                result.multiHandWorldLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);
        Log.i(
                TAG,
                String.format(
                        "MediaPipe Hand wrist world coordinates (in meters with the origin at the hand's"
                                + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                        wristWorldLandmark.getX(), wristWorldLandmark.getY(), wristWorldLandmark.getZ()));
    }


}
