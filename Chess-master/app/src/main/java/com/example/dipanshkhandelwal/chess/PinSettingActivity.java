package com.example.dipanshkhandelwal.chess;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.formats.proto.LandmarkProto.Landmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// ContentResolver dependency

public class PinSettingActivity extends AppCompatActivity {
    private IOHelper ioHelper;
    private String newUser;
    private String newEmail;
    private String newPassword;
    private String newAccount;
    private int consecutiveFrames=10;

    private static final String TAG = "PinSettingActivity";
    private Hands hands;
    // Run the pipeline and the model inference on GPU or CPU.
    private static final boolean RUN_ON_GPU = true;

    // Live camera demo UI and camera components.
    private CameraInput cameraInput;

    private SolutionGlSurfaceView<HandsResult> glSurfaceView;

    private List<ArrayList<Integer>> lmList = new ArrayList<>();
    private List<Integer> tipIds = new ArrayList<>(Arrays.asList(4, 8, 12, 16, 20));
    private enum HandOrientation {
        UNKNOWN,
        FRONT,
        BACK
    }
    private HandOrientation handOrientation = HandOrientation.UNKNOWN;
    private Integer gestureCounter = 0;
    private Integer gestureHolder = null;
    private ArrayList<Integer> userPsw = new ArrayList<>();
    private Map<Integer, String> gestureMap = new HashMap<Integer, String>() {{
        put(0, "zero");
        put(1, "uno");
        put(2, "due");
        put(3, "tre");
        put(4, "quattro");
        put(5, "cinque");
        put(6, "rock");
        put(7, "yo");
        put(8, "okay");
    }};
    private Map<Integer, Integer> gestureImgId = new HashMap<Integer, Integer>() {{
        put(0, R.drawable.pugno);
        put(1, R.drawable.uno);
        put(2, R.drawable.due);
        put(3, R.drawable.tre);
        put(4, R.drawable.quattro);
        put(5, R.drawable.cinque);
        put(6, R.drawable.rock);
        put(7, R.drawable.yo);
        put(8, R.drawable.ok);
    }};
    private Boolean isChoosing = false;
    private AlertDialog choiceDialog;
    private AlertDialog.Builder builder;
    private String[] choices = {"Si", "No"};
    private boolean inFinalCall=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setting);
        ioHelper=new IOHelper(getApplicationContext(),"accounts.json");
        newUser=getIntent().getStringExtra("newUser");
        newEmail=getIntent().getStringExtra("newEmail");
        newPassword=getIntent().getStringExtra("newPassword");
        newAccount=getIntent().getStringExtra("newAccount");
        ioHelper=new IOHelper(getApplicationContext(),"accounts.json");

        setupLiveDemoUiComponents();
        builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isChoosing = false;
                if (choices[which].equals("Si")){
                    userPsw.add(gestureHolder);
                    Integer imageIndex = userPsw.size() - 1;
                    ImageView gestureImage;
                    switch(imageIndex) {
                        case 0:
                            gestureImage = findViewById(R.id.img0);
                            gestureImage.setImageResource(gestureImgId.get(gestureHolder));
                            break;
                        case 1:
                            gestureImage = findViewById(R.id.img1);
                            gestureImage.setImageResource(gestureImgId.get(gestureHolder));
                            break;
                        case 2:
                            gestureImage = findViewById(R.id.img2);
                            gestureImage.setImageResource(gestureImgId.get(gestureHolder));
                            break;
                        case 3:
                            gestureImage = findViewById(R.id.img3);
                            gestureImage.setImageResource(gestureImgId.get(gestureHolder));
                            break;
                    }
                }
                choiceDialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restarts the camera and the opengl surface rendering.
        cameraInput = new CameraInput(this);
        cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
        glSurfaceView.post(this::startCamera);
        glSurfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.setVisibility(View.GONE);
        cameraInput.close();
    }

    /** Sets up the UI components for the live demo with camera input. */
    private void setupLiveDemoUiComponents() {
        stopCurrentPipeline();
        setupStreamingModePipeline();
    }

    /** Sets up core workflow for streaming mode. */
    private void setupStreamingModePipeline() {

        // Initializes a new MediaPipe Hands solution instance in the streaming mode.
        hands =
                new Hands(
                        this,
                        HandsOptions.builder()
                                .setStaticImageMode(false)
                                .setMaxNumHands(2)
                                .setRunOnGpu(RUN_ON_GPU)
                                .build());
        hands.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));

        cameraInput = new CameraInput(this);
        cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));

        // Initializes a new Gl surface view with a user-defined HandsResultGlRenderer.
        glSurfaceView =
                new SolutionGlSurfaceView<>(this, hands.getGlContext(), hands.getGlMajorVersion());
        glSurfaceView.setSolutionResultRenderer(new HandsResultGlRenderer());
        glSurfaceView.setRenderInputImage(true);

        hands.setResultListener(
                handsResult -> {
                    if (!isChoosing && !inFinalCall) {
                        lmList = findPosition(handsResult, 0);
                        getHandOrientation();
                        ArrayList<Integer> fingers = fingersUp();
                        if (userPsw.size() < 4) {
                            Integer currentGesture = getGestureCode(fingers, handsResult);
                            if (currentGesture == gestureHolder && currentGesture != null){
                                if (gestureCounter == consecutiveFrames){
                                    builder.setTitle("Confermi la gesture " + gestureMap.get(gestureHolder) + "?");
                                    isChoosing = true;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            choiceDialog = builder.create();
                                            choiceDialog.setCanceledOnTouchOutside(false);
                                            choiceDialog.show();
                                        }
                                    });
                                    gestureCounter = 0;
                                    gestureHolder = null;
                                } else {
                                    gestureCounter ++;
                                }
                            } else {
                                gestureCounter = 0;
                            }
                            gestureHolder = currentGesture;
                        } else {
                            inFinalCall=true;

                            StringBuilder strbul  = new StringBuilder();
                            Iterator<Integer> iter = userPsw.iterator();
                            while(iter.hasNext())
                            {
                                strbul.append(iter.next());
                            }
                            //TODO stringCode è la stringa da salvare
                            String stringCode = strbul.toString();
                            try{
                            JSONObject jsonNewAccount=new JSONObject(newAccount);

                            addPin(jsonNewAccount,stringCode);
                            addAccount(jsonNewAccount);
                            }
                            catch (Exception e){e.printStackTrace();}


                           // stopCurrentPipeline();
                            goMessage();
                            finish();

                        }
                    }
                    logWristLandmark(handsResult, /*showPixelValues=*/ false);
                    glSurfaceView.setRenderData(handsResult);
                    glSurfaceView.requestRender();
                });

        // The runnable to start camera after the gl surface view is attached.
        // For video input source, videoInput.start() will be called when the video uri is available.
        glSurfaceView.post(this::startCamera);


        // Updates the preview layout.
        FrameLayout frameLayout = findViewById(R.id.preview_display_layout);

        frameLayout.removeAllViewsInLayout();
        frameLayout.addView(glSurfaceView);
        // make camera layout invisible
        glSurfaceView.setVisibility(View.VISIBLE);
        frameLayout.requestLayout();}



    private List<ArrayList<Integer>> findPosition(HandsResult result, Integer handNo){
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


    public Integer getGestureCode(ArrayList<Integer> fingers, HandsResult result){

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

    private void getHandOrientation() {
        if (!lmList.isEmpty()) {
            // Check if the x value of 5th landmark is less than the 17th landmark in order to
            // recognize the hand orientation
            if (lmList.get(5).get(1) < lmList.get(17).get(1)) {
                handOrientation = HandOrientation.FRONT;
            } else {
                handOrientation = HandOrientation.BACK;
            }
        }
        else {
            handOrientation = HandOrientation.UNKNOWN;
        }
    }

    private ArrayList<Integer> fingersUp(){
        if(!lmList.isEmpty()){
            ArrayList<Integer> fingers = new ArrayList<>();
            if (handOrientation == HandOrientation.FRONT){

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

    private void startCamera() {
        cameraInput.start(
                this,
                hands.getGlContext(),
                CameraInput.CameraFacing.FRONT,
                glSurfaceView.getWidth(),
                glSurfaceView.getHeight());
    }

    private void stopCurrentPipeline() {
        if (cameraInput != null) {
            cameraInput.setNewFrameListener(null);
            cameraInput.close();
        }
        try {
            if (glSurfaceView != null) {
                glSurfaceView.setVisibility(View.GONE);
            }
        } catch (Exception e){
            // hide the glSurfaceView
            runOnUiThread(new Runnable() {
                public void run() {
                    glSurfaceView.setVisibility(View.GONE);
                }
            });
        }
        if (hands != null) {
            hands.close();
        }
    }

    private void showToast(Context ctx, String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
    }

    private void logWristLandmark(HandsResult result, boolean showPixelValues) {
        // If no landmarks are detected
        if (result.multiHandLandmarks().isEmpty()) {
            return;
        }

        NormalizedLandmark wristLandmark =
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
        Landmark wristWorldLandmark =
                result.multiHandWorldLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);
        Log.i(
                TAG,
                String.format(
                        "MediaPipe Hand wrist world coordinates (in meters with the origin at the hand's"
                                + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                        wristWorldLandmark.getX(), wristWorldLandmark.getY(), wristWorldLandmark.getZ()));
    }

    private void addAccount(JSONObject account){
        String stringAccounts=ioHelper.stringFromFile();
        try {
            JSONArray accountArray = new JSONArray(stringAccounts);
            accountArray.put(account);
            ioHelper.writeToFile(accountArray.toString());
        }
        catch (Exception e){e.printStackTrace();
            finish();}
    }
    private void addPin(JSONObject account,String pin){
        try{
        account.put("pin",pin);}
        catch (Exception e){
          e.printStackTrace();
            finish();

        }
    }
    private void goMessage(){

        Intent intent = new Intent(getBaseContext(), SignUpConfirmation.class);
        this.startActivity(intent);

    }



}