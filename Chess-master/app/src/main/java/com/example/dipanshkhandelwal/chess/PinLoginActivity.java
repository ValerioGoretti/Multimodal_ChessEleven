package com.example.dipanshkhandelwal.chess;

import androidx.appcompat.app.AppCompatActivity;

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

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PinLoginActivity extends AppCompatActivity {
    IOHelper ioHelper;
    private String pin;
    private int consecutiveFrames=10;
    private static final String TAG = "PinLoginActivity";
    private Hands hands;
    // Run the pipeline and the model inference on GPU or CPU.
    private static final boolean RUN_ON_GPU = true;
    private boolean inFinalCall=false;
    // Live camera demo UI and camera components.
    private CameraInput cameraInput;

    private SolutionGlSurfaceView<HandsResult> glSurfaceView;

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
    private String user;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setting);
        ioHelper=new IOHelper(getApplicationContext(),"accounts.json");
        user=getIntent().getStringExtra("User");
        password=getIntent().getStringExtra("Password");
        pin=getIntent().getStringExtra("Pin");
        Toast.makeText(getApplicationContext(), user+password+pin,
                Toast.LENGTH_LONG).show();

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
                        List<ArrayList<Integer>> lmList = GestureRecognition.findPosition(handsResult, 0);
                        String handOrientation = GestureRecognition.getHandOrientation(lmList);
                        ArrayList<Integer> fingers = GestureRecognition.fingersUp(lmList, handOrientation);

                        if (userPsw.size() < 4) {
                            Integer currentGesture = GestureRecognition.getGestureCode(fingers, handsResult);
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
                            String stringCode = strbul.toString();

                            if(stringCode.equals(pin)){
                                Intent intent = new Intent(getBaseContext(), Home.class);
                                Log.i("PinLogin", "USERNAME -->  " + user);
                                intent.putExtra("user", user);
                                SessionManager sessionManager=new SessionManager(this);
                                sessionManager.login(user,password,pin);

                                startActivity(intent);
                                stopCurrentPipeline();
                            } else {
                                // set all fields to default and retry
                                gestureCounter = 0;
                                gestureHolder = null;
                                userPsw.clear();
                                inFinalCall = false;
                                // Clear al images
                                ImageView gestureImage;
                                gestureImage = findViewById(R.id.img0);
                                gestureImage.setImageResource(0);
                                gestureImage = findViewById(R.id.img1);
                                gestureImage.setImageResource(0);
                                gestureImage = findViewById(R.id.img2);
                                gestureImage.setImageResource(0);
                                gestureImage = findViewById(R.id.img3);
                                gestureImage.setImageResource(0);
                            }
                        }
                    }
                    GestureRecognition.logWristLandmark(handsResult, /*showPixelValues=*/ false, TAG);
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
        frameLayout.requestLayout();
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

}