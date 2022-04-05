package com.example.dipanshkhandelwal.chess;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
public class MainActivity extends AppCompatActivity implements View.OnClickListener, RecognitionListener {
    public Square c1=null;
    public Square c2=null;
    public Square click = null;
    public TextView game_over;
    public TextView[][] DisplayBoard = new TextView[8][8];
    public TextView[][] DisplayBoardBackground = new TextView[8][8];
    public TextView[][] DisplayBoardBackgroundSelected = new TextView[8][8];
    public LinearLayout pawn_choices;
    public Board board;
    private Piece lastChoice=null;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView returnedText;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private boolean isTriggered=false;
    private CountDownTimer timer;
    private MediaPlayer listeningSound;
    private MediaPlayer doneSound;
    private MediaPlayer errorSound;
    private TextToCommand textToCommand=new TextToCommand();
    private int currentTask=0;
    private int currentStep=0;
    private Move proposedMove;
    private ImageView imlistenig;
    private Suggestions suggestions=new Suggestions();
    private Player player= new Player();
    private boolean ismyturn=true;
    private LinearLayout settingsMenu ;
    private TextToSpeech tt;




    private void resetSpeechRecognizer() {
        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if(SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();
    }
    private void setRecogniserIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "it");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_main);
        initializeBoard();
        imlistenig=(ImageView) findViewById(R.id.imlistening);
        listeningSound=MediaPlayer.create(this,R.raw.listening);
        doneSound=MediaPlayer.create(this,R.raw.done);
        errorSound=MediaPlayer.create(this,R.raw.error);
        game_over = (TextView) findViewById(R.id.game_over);
        pawn_choices = (LinearLayout) findViewById(R.id.pawn_chioces);
        game_over.setVisibility(View.INVISIBLE);
        pawn_choices.setVisibility(View.INVISIBLE);
        settingsMenu=(LinearLayout) findViewById(R.id.settingsMenu);
        returnedText =(TextView) findViewById(R.id.textAssistent);
        resetSpeechRecognizer();
        timer=new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                if (isTriggered==false){cancel();}
            }
            @Override
            public void onFinish() {

                if(isTriggered){errorSound.start();}
                isTriggered=false;
                if(currentTask==0){
                    errorSound.start();
                    returnedText.setText("I didn't understand the first command! Please try again");
                    new CountDownTimer(3000,1000){
                        @Override
                        public void onTick(long l) {

                        }
                        @Override
                        public void onFinish() {
                            returnedText.setText(suggestions.getFirstMessage());
                            imlistenig.setVisibility(View.INVISIBLE);
                        }
                    }.start();
                }

            }
        };
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        setRecogniserIntent();
        speech.startListening(recognizerIntent);

        tt = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                    tt.setLanguage(Locale.ENGLISH);
                    tt.setSpeechRate((float)1);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent);
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                        .LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "resume");
        super.onResume();
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }
    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "pause");
        super.onPause();
        speech.stopListening();
    }
    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
    }
    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");

    }
    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }
    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        speech.stopListening();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        HashSet<String> res=new HashSet(matches);
        String text = "";
        if (matches!=null) {
            for(String t:matches){
                text+=t+" ";
            }
            if(currentTask==0){
                if (res.contains("assistente") || res.contains("Assistente")) {
                    if (!isTriggered) {
                        isTriggered = true;
                        timer.start();
                        listeningSound.start();
                        imlistenig.setVisibility(View.VISIBLE);

                    }
                } else {
                    if (isTriggered) {
                        String result=textToCommand.getTriggerCommand(text.toLowerCase());
                        isTriggered = false;
                        Boolean found=false;
                        if (result.equals("What move do you want to do?")){
                            found=true;
                        String mossa = textToCommand.getMove(text.toLowerCase());
                         if (!mossa.equals("I didn't understand the move")) {
                            Move m = searchMove(mossa);
                            isTriggered=false;

                            if (m == null) {
                                returnedText.setText("Uncorrect move! Try again");
                                errorSound.start();
                                new CountDownTimer(3000, 1000) {
                                    @Override
                                    public void onTick(long l) {
                                    }
                                    @Override
                                    public void onFinish() {
                                        returnedText.setText(suggestions.getFirstMessage());
                                        imlistenig.setVisibility(View.GONE);

                                    }
                                }.start();
                            } else {
                                System.out.println("Correct Move! " + m.toString());
                                System.out.println("Piece moved  " + board.getPiece(m.getFrom()));
                                proposedMove = m;
                                String sentence= "Do you confirm the move:" + board.getPiece(m.getFrom()).toString().toLowerCase().replace("_", " ") + " from " + m.getFrom() + " to " + m.getTo() + "?";
                                speak(sentence);
                                returnedText.setText("Do you confirm the move:\n" + board.getPiece(m.getFrom()).toString().toLowerCase().replace("_", " ") + " from " + m.getFrom() + " to " + m.getTo() + "?\n\n'si'\n\n'no'");
                                currentTask = 1;
                                currentStep = 2;
                            }
                        }}


                        else if(result.equals("What kind of help do you want?\n\n'Dimmi le mosse per il pedone in c2'\n\n'Esegui la miglior mossa possibile'\n\n'indietro'")){
                            returnedText.setText(result);
                            found=true;
                            currentTask=2;
                            speak("What kind of help do you want?");
                        }
                        else if(result.equals("screen")){
                            startActivity(new Intent("android.settings.CAST_SETTINGS"));
                            found=true;
                        }
                        else if(result.equals("What command do you want to do?")){
                            currentTask=3;
                            found=true;
                            returnedText.setText("What command do you want to do?\n\n'Ricomincia la partita'\n'Esci dall'applicazione'\n\n'indietro'");
                            settingsMenu.setVisibility(View.VISIBLE);
                            imlistenig.setVisibility(View.VISIBLE);
                            speak(result);

                        }
                        else if (!found){

                            isTriggered=false;
                            returnedText.setText("I didn't understand the first command! Please try again");
                            errorSound.start();
                            new CountDownTimer(3000,1000){
                                @Override
                                public void onTick(long l) {

                                }
                                @Override
                                public void onFinish() {
                                    returnedText.setText(suggestions.getFirstMessage());
                                    imlistenig.setVisibility(View.INVISIBLE);
                                }
                            }.start();
                        }
                    }
                }

            }
            else{
                if(text.toLowerCase().contains("indietro")&&(currentTask!=3)){returnedText.setText(" ");currentTask=0;imlistenig.setVisibility(View.GONE);returnedText.setText(suggestions.getFirstMessage());errorSound.start();}
                if(currentTask==10){
                       String promozione=text.toLowerCase();
                       View v=null;
                           if (promozione.contains("regina")){v = findViewById(getResources().getIdentifier("pawn_queen","id", getBaseContext().getPackageName()));
                                         }
                          else if(promozione.contains("torre")){  v = findViewById(getResources().getIdentifier("pawn_rock","id", getBaseContext().getPackageName()));
                                          }
                          else if(promozione.contains("cavallo")){ v =findViewById(getResources().getIdentifier("pawn_knight","id", getBaseContext().getPackageName()));
                                        }
                          else if(promozione.contains("alfiere")) {
                               v = findViewById(getResources().getIdentifier("pawn_bishop", "id", getBaseContext().getPackageName()));
                           }
                       if (v==null){returnedText.setText("\n Please chose among:\n'Regina'\n'Alfiere'\n'Torre'\n'Cavallo'\n");errorSound.start();}
                       else{returnedText.setText(suggestions.getFirstMessage());currentTask=0;currentStep=0;pawnChoice(v);proposedMove=null;imlistenig.setVisibility(View.GONE);imlistenig.setVisibility(View.INVISIBLE);doneSound.start();}
                }
                if (currentTask==1){
                    if (currentStep==2 && proposedMove!=null){
                        System.out.println(text.toLowerCase());
                        if(text.toLowerCase().contains("sì")){
                            currentTask=0;
                            currentStep=0;
                            System.out.println("Task 1. ");
                            returnedText.setText(suggestions.getFirstMessage());
                            List<Integer> coordinate_from=parseMove(proposedMove.getFrom());
                            String coordinate_f="R"+coordinate_from.get(0) +""+coordinate_from.get(1);
                            View from=findViewById(getResources().getIdentifier(coordinate_f,"id", getBaseContext().getPackageName()));
                            onClick(from);
                            List<Integer> coordinate_to=parseMove(proposedMove.getTo());
                            String coordinate_t="R"+coordinate_to.get(0) +""+coordinate_to.get(1);
                            View to=findViewById(getResources().getIdentifier(coordinate_t,"id", getBaseContext().getPackageName()));
                            onClick(to);
                            proposedMove=null;
                            imlistenig.setVisibility(View.GONE);
                            doneSound.start();
                        }
                        if(text.toLowerCase().contains("no")){
                            currentStep=0;
                            currentTask=0;
                            returnedText.setText(suggestions.getFirstMessage());
                            imlistenig.setVisibility(View.GONE);
                            errorSound.start();
                        }
                    }
                }
                if (currentTask==2){
                    if (currentStep==0){
                        String cell = textToCommand.getCell(text.toLowerCase());
                        if (!cell.equals("I didn't understand")){
                            Square s=Square.fromValue(cell.toUpperCase().replaceAll("\\s",""));
                            if(cellMoves(s).size()!=0){
                                clearDuble();
                                clearBoardColor();
                                Piece piece=board.getPiece(s);
                                returnedText.setText("Here the possible moves for the "+piece.toString().replace("_"," ")+" in "+cell.toUpperCase());
                                List<Integer> coordinate=parseMove(s);
                                String click="R"+coordinate.get(0)+""+coordinate.get(1);
                                View viewCella=findViewById(getResources().getIdentifier(click,"id",getBaseContext().getPackageName()));
                                onClick(viewCella);
                                doneSound.start();
                                new CountDownTimer(3000,1000){
                                    @Override
                                    public void onTick(long l) {

                                    }
                                    @Override
                                    public void onFinish() {
                                        returnedText.setText(suggestions.getFirstMessage());
                                        imlistenig.setVisibility(View.INVISIBLE);
                                        currentTask=0;
                                        currentStep=0;
                                    }
                                }.start();

                            }
                            else{
                                returnedText.setText("No moves for the cell "+cell.toUpperCase());
                                errorSound.start();

                            }


                        }


                        else if(textToCommand.isInBestMoves(text.toLowerCase()))
                        {
                            Move mo = player.eseguiMossa(board.legalMoves());
                            List<Integer> coordinate_from=parseMove(mo.getFrom());
                            String coordinate_f="R"+coordinate_from.get(0) +""+coordinate_from.get(1);
                            View from=findViewById(getResources().getIdentifier(coordinate_f,"id", getBaseContext().getPackageName()));
                            onClick(from);
                            List<Integer> coordinate_to=parseMove(mo.getTo());
                            String coordinate_t="R"+coordinate_to.get(0) +""+coordinate_to.get(1);
                            View to=findViewById(getResources().getIdentifier(coordinate_t,"id", getBaseContext().getPackageName()));
                            onClick(to);
                            doneSound.start();
                            returnedText.setText("Best possible move executed!");
                            new CountDownTimer(3000,1000){
                                @Override
                                public void onTick(long l) {

                                }
                                @Override
                                public void onFinish() {
                                    returnedText.setText(suggestions.getFirstMessage());
                                    imlistenig.setVisibility(View.INVISIBLE);
                                    currentTask=0;
                                    currentStep=0;
                                }
                            }.start();
                        }

                    }
                }
                if(currentTask==3){
                    if(textToCommand.isFinish(text.toLowerCase())){
                        finish();
                    }
                    else if (textToCommand.isRestart(text.toLowerCase())){
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                    else if(text.toLowerCase().contains("indietro")){
                        settingsMenu.setVisibility(View.GONE);
                        currentTask=0;
                        currentStep=0;
                        returnedText.setText(suggestions.getFirstMessage());
                        imlistenig.setVisibility(View.GONE);
                        errorSound.start();
                    }
                }
            }
        }
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(LOG_TAG, "FAILED " + errorMessage);
        System.out.println("FAILED "+errorMessage);
        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }
    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }
    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }
    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }



    private void initializeBoard() {
        board = new Board();

        DisplayBoard[0][0] = (TextView) findViewById(R.id.R00);
        DisplayBoard[0][0].setBackgroundResource(R.drawable.wrook);
        DisplayBoard[1][0] = (TextView) findViewById(R.id.R10);
        DisplayBoard[1][0].setBackgroundResource(R.drawable.wknight);
        DisplayBoard[2][0] = (TextView) findViewById(R.id.R20);
        DisplayBoard[2][0].setBackgroundResource(R.drawable.wbishop);
        DisplayBoard[3][0] = (TextView) findViewById(R.id.R30);
        DisplayBoard[3][0].setBackgroundResource(R.drawable.wqueen);
        DisplayBoard[4][0] = (TextView) findViewById(R.id.R40);
        DisplayBoard[4][0].setBackgroundResource(R.drawable.wking);
        DisplayBoard[5][0] = (TextView) findViewById(R.id.R50);
        DisplayBoard[5][0].setBackgroundResource(R.drawable.wbishop);
        DisplayBoard[6][0] = (TextView) findViewById(R.id.R60);
        DisplayBoard[6][0].setBackgroundResource(R.drawable.wknight);
        DisplayBoard[7][0] = (TextView) findViewById(R.id.R70);
        DisplayBoard[7][0].setBackgroundResource(R.drawable.wrook);


        DisplayBoard[0][1] = (TextView) findViewById(R.id.R01);
        DisplayBoard[0][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[1][1] = (TextView) findViewById(R.id.R11);
        DisplayBoard[1][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[2][1] = (TextView) findViewById(R.id.R21);
        DisplayBoard[2][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[3][1] = (TextView) findViewById(R.id.R31);
        DisplayBoard[3][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[4][1] = (TextView) findViewById(R.id.R41);
        DisplayBoard[4][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[5][1] = (TextView) findViewById(R.id.R51);
        DisplayBoard[5][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[6][1] = (TextView) findViewById(R.id.R61);
        DisplayBoard[6][1].setBackgroundResource(R.drawable.wpawn);
        DisplayBoard[7][1] = (TextView) findViewById(R.id.R71);
        DisplayBoard[7][1].setBackgroundResource(R.drawable.wpawn);

        DisplayBoard[0][2] = (TextView) findViewById(R.id.R02);
        DisplayBoard[1][2] = (TextView) findViewById(R.id.R12);
        DisplayBoard[2][2] = (TextView) findViewById(R.id.R22);
        DisplayBoard[3][2] = (TextView) findViewById(R.id.R32);
        DisplayBoard[4][2] = (TextView) findViewById(R.id.R42);
        DisplayBoard[5][2] = (TextView) findViewById(R.id.R52);
        DisplayBoard[6][2] = (TextView) findViewById(R.id.R62);
        DisplayBoard[7][2] = (TextView) findViewById(R.id.R72);
        DisplayBoard[0][3] = (TextView) findViewById(R.id.R03);
        DisplayBoard[1][3] = (TextView) findViewById(R.id.R13);
        DisplayBoard[2][3] = (TextView) findViewById(R.id.R23);
        DisplayBoard[3][3] = (TextView) findViewById(R.id.R33);
        DisplayBoard[4][3] = (TextView) findViewById(R.id.R43);
        DisplayBoard[5][3] = (TextView) findViewById(R.id.R53);
        DisplayBoard[6][3] = (TextView) findViewById(R.id.R63);
        DisplayBoard[7][3] = (TextView) findViewById(R.id.R73);
        DisplayBoard[0][4] = (TextView) findViewById(R.id.R04);
        DisplayBoard[1][4] = (TextView) findViewById(R.id.R14);
        DisplayBoard[2][4] = (TextView) findViewById(R.id.R24);
        DisplayBoard[3][4] = (TextView) findViewById(R.id.R34);
        DisplayBoard[4][4] = (TextView) findViewById(R.id.R44);
        DisplayBoard[5][4] = (TextView) findViewById(R.id.R54);
        DisplayBoard[6][4] = (TextView) findViewById(R.id.R64);
        DisplayBoard[7][4] = (TextView) findViewById(R.id.R74);
        DisplayBoard[0][5] = (TextView) findViewById(R.id.R05);
        DisplayBoard[1][5] = (TextView) findViewById(R.id.R15);
        DisplayBoard[2][5] = (TextView) findViewById(R.id.R25);
        DisplayBoard[3][5] = (TextView) findViewById(R.id.R35);
        DisplayBoard[4][5] = (TextView) findViewById(R.id.R45);
        DisplayBoard[5][5] = (TextView) findViewById(R.id.R55);
        DisplayBoard[6][5] = (TextView) findViewById(R.id.R65);
        DisplayBoard[7][5] = (TextView) findViewById(R.id.R75);
        DisplayBoard[0][6] = (TextView) findViewById(R.id.R06);
        DisplayBoard[0][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[1][6] = (TextView) findViewById(R.id.R16);
        DisplayBoard[1][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[2][6] = (TextView) findViewById(R.id.R26);
        DisplayBoard[2][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[3][6] = (TextView) findViewById(R.id.R36);
        DisplayBoard[3][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[4][6] = (TextView) findViewById(R.id.R46);
        DisplayBoard[4][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[5][6] = (TextView) findViewById(R.id.R56);
        DisplayBoard[5][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[6][6] = (TextView) findViewById(R.id.R66);
        DisplayBoard[6][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[7][6] = (TextView) findViewById(R.id.R76);
        DisplayBoard[7][6].setBackgroundResource(R.drawable.bpawn);
        DisplayBoard[0][7] = (TextView) findViewById(R.id.R07);
        DisplayBoard[0][7].setBackgroundResource(R.drawable.brook);
        DisplayBoard[1][7] = (TextView) findViewById(R.id.R17);
        DisplayBoard[1][7].setBackgroundResource(R.drawable.bknight);
        DisplayBoard[2][7] = (TextView) findViewById(R.id.R27);
        DisplayBoard[2][7].setBackgroundResource(R.drawable.bbishop);
        DisplayBoard[3][7] = (TextView) findViewById(R.id.R37);
        DisplayBoard[3][7].setBackgroundResource(R.drawable.bqueen);
        DisplayBoard[4][7] = (TextView) findViewById(R.id.R47);
        DisplayBoard[4][7].setBackgroundResource(R.drawable.bking);
        DisplayBoard[5][7] = (TextView) findViewById(R.id.R57);
        DisplayBoard[5][7].setBackgroundResource(R.drawable.bbishop);
        DisplayBoard[6][7] = (TextView) findViewById(R.id.R67);
        DisplayBoard[6][7].setBackgroundResource(R.drawable.bknight);
        DisplayBoard[7][7] = (TextView) findViewById(R.id.R77);
        DisplayBoard[7][7].setBackgroundResource(R.drawable.brook);

        DisplayBoardBackground[0][0] = (TextView) findViewById(R.id.R000);
        DisplayBoardBackground[1][0] = (TextView) findViewById(R.id.R010);
        DisplayBoardBackground[2][0] = (TextView) findViewById(R.id.R020);
        DisplayBoardBackground[3][0] = (TextView) findViewById(R.id.R030);
        DisplayBoardBackground[4][0] = (TextView) findViewById(R.id.R040);
        DisplayBoardBackground[5][0] = (TextView) findViewById(R.id.R050);
        DisplayBoardBackground[6][0] = (TextView) findViewById(R.id.R060);
        DisplayBoardBackground[7][0] = (TextView) findViewById(R.id.R070);

        DisplayBoardBackground[0][1] = (TextView) findViewById(R.id.R001);
        DisplayBoardBackground[1][1] = (TextView) findViewById(R.id.R011);
        DisplayBoardBackground[2][1] = (TextView) findViewById(R.id.R021);
        DisplayBoardBackground[3][1] = (TextView) findViewById(R.id.R031);
        DisplayBoardBackground[4][1] = (TextView) findViewById(R.id.R041);
        DisplayBoardBackground[5][1] = (TextView) findViewById(R.id.R051);
        DisplayBoardBackground[6][1] = (TextView) findViewById(R.id.R061);
        DisplayBoardBackground[7][1] = (TextView) findViewById(R.id.R071);

        DisplayBoardBackground[0][2] = (TextView) findViewById(R.id.R002);
        DisplayBoardBackground[1][2] = (TextView) findViewById(R.id.R012);
        DisplayBoardBackground[2][2] = (TextView) findViewById(R.id.R022);
        DisplayBoardBackground[3][2] = (TextView) findViewById(R.id.R032);
        DisplayBoardBackground[4][2] = (TextView) findViewById(R.id.R042);
        DisplayBoardBackground[5][2] = (TextView) findViewById(R.id.R052);
        DisplayBoardBackground[6][2] = (TextView) findViewById(R.id.R062);
        DisplayBoardBackground[7][2] = (TextView) findViewById(R.id.R072);

        DisplayBoardBackground[0][3] = (TextView) findViewById(R.id.R003);
        DisplayBoardBackground[1][3] = (TextView) findViewById(R.id.R013);
        DisplayBoardBackground[2][3] = (TextView) findViewById(R.id.R023);
        DisplayBoardBackground[3][3] = (TextView) findViewById(R.id.R033);
        DisplayBoardBackground[4][3] = (TextView) findViewById(R.id.R043);
        DisplayBoardBackground[5][3] = (TextView) findViewById(R.id.R053);
        DisplayBoardBackground[6][3] = (TextView) findViewById(R.id.R063);
        DisplayBoardBackground[7][3] = (TextView) findViewById(R.id.R073);

        DisplayBoardBackground[0][4] = (TextView) findViewById(R.id.R004);
        DisplayBoardBackground[1][4] = (TextView) findViewById(R.id.R014);
        DisplayBoardBackground[2][4] = (TextView) findViewById(R.id.R024);
        DisplayBoardBackground[3][4] = (TextView) findViewById(R.id.R034);
        DisplayBoardBackground[4][4] = (TextView) findViewById(R.id.R044);
        DisplayBoardBackground[5][4] = (TextView) findViewById(R.id.R054);
        DisplayBoardBackground[6][4] = (TextView) findViewById(R.id.R064);
        DisplayBoardBackground[7][4] = (TextView) findViewById(R.id.R074);

        DisplayBoardBackground[0][5] = (TextView) findViewById(R.id.R005);
        DisplayBoardBackground[1][5] = (TextView) findViewById(R.id.R015);
        DisplayBoardBackground[2][5] = (TextView) findViewById(R.id.R025);
        DisplayBoardBackground[3][5] = (TextView) findViewById(R.id.R035);
        DisplayBoardBackground[4][5] = (TextView) findViewById(R.id.R045);
        DisplayBoardBackground[5][5] = (TextView) findViewById(R.id.R055);
        DisplayBoardBackground[6][5] = (TextView) findViewById(R.id.R065);
        DisplayBoardBackground[7][5] = (TextView) findViewById(R.id.R075);

        DisplayBoardBackground[0][6] = (TextView) findViewById(R.id.R006);
        DisplayBoardBackground[1][6] = (TextView) findViewById(R.id.R016);
        DisplayBoardBackground[2][6] = (TextView) findViewById(R.id.R026);
        DisplayBoardBackground[3][6] = (TextView) findViewById(R.id.R036);
        DisplayBoardBackground[4][6] = (TextView) findViewById(R.id.R046);
        DisplayBoardBackground[5][6] = (TextView) findViewById(R.id.R056);
        DisplayBoardBackground[6][6] = (TextView) findViewById(R.id.R066);
        DisplayBoardBackground[7][6] = (TextView) findViewById(R.id.R076);

        DisplayBoardBackground[0][7] = (TextView) findViewById(R.id.R007);
        DisplayBoardBackground[1][7] = (TextView) findViewById(R.id.R017);
        DisplayBoardBackground[2][7] = (TextView) findViewById(R.id.R027);
        DisplayBoardBackground[3][7] = (TextView) findViewById(R.id.R037);
        DisplayBoardBackground[4][7] = (TextView) findViewById(R.id.R047);
        DisplayBoardBackground[5][7] = (TextView) findViewById(R.id.R057);
        DisplayBoardBackground[6][7] = (TextView) findViewById(R.id.R067);
        DisplayBoardBackground[7][7] = (TextView) findViewById(R.id.R077);

        /*
        -----------------
         */
        DisplayBoardBackgroundSelected[0][0] = (TextView) findViewById(R.id.RO000);
        DisplayBoardBackgroundSelected[1][0] = (TextView) findViewById(R.id.RO010);
        DisplayBoardBackgroundSelected[2][0] = (TextView) findViewById(R.id.RO020);
        DisplayBoardBackgroundSelected[3][0] = (TextView) findViewById(R.id.RO030);
        DisplayBoardBackgroundSelected[4][0] = (TextView) findViewById(R.id.RO040);
        DisplayBoardBackgroundSelected[5][0] = (TextView) findViewById(R.id.RO050);
        DisplayBoardBackgroundSelected[6][0] = (TextView) findViewById(R.id.RO060);
        DisplayBoardBackgroundSelected[7][0] = (TextView) findViewById(R.id.RO070);

        DisplayBoardBackgroundSelected[0][1] = (TextView) findViewById(R.id.RO001);
        DisplayBoardBackgroundSelected[1][1] = (TextView) findViewById(R.id.RO011);
        DisplayBoardBackgroundSelected[2][1] = (TextView) findViewById(R.id.RO021);
        DisplayBoardBackgroundSelected[3][1] = (TextView) findViewById(R.id.RO031);
        DisplayBoardBackgroundSelected[4][1] = (TextView) findViewById(R.id.RO041);
        DisplayBoardBackgroundSelected[5][1] = (TextView) findViewById(R.id.RO051);
        DisplayBoardBackgroundSelected[6][1] = (TextView) findViewById(R.id.RO061);
        DisplayBoardBackgroundSelected[7][1] = (TextView) findViewById(R.id.RO071);

        DisplayBoardBackgroundSelected[0][2] = (TextView) findViewById(R.id.RO002);
        DisplayBoardBackgroundSelected[1][2] = (TextView) findViewById(R.id.RO012);
        DisplayBoardBackgroundSelected[2][2] = (TextView) findViewById(R.id.RO022);
        DisplayBoardBackgroundSelected[3][2] = (TextView) findViewById(R.id.RO032);
        DisplayBoardBackgroundSelected[4][2] = (TextView) findViewById(R.id.RO042);
        DisplayBoardBackgroundSelected[5][2] = (TextView) findViewById(R.id.RO052);
        DisplayBoardBackgroundSelected[6][2] = (TextView) findViewById(R.id.RO062);
        DisplayBoardBackgroundSelected[7][2] = (TextView) findViewById(R.id.RO072);

        DisplayBoardBackgroundSelected[0][3] = (TextView) findViewById(R.id.RO003);
        DisplayBoardBackgroundSelected[1][3] = (TextView) findViewById(R.id.RO013);
        DisplayBoardBackgroundSelected[2][3] = (TextView) findViewById(R.id.RO023);
        DisplayBoardBackgroundSelected[3][3] = (TextView) findViewById(R.id.RO033);
        DisplayBoardBackgroundSelected[4][3] = (TextView) findViewById(R.id.RO043);
        DisplayBoardBackgroundSelected[5][3] = (TextView) findViewById(R.id.RO053);
        DisplayBoardBackgroundSelected[6][3] = (TextView) findViewById(R.id.RO063);
        DisplayBoardBackgroundSelected[7][3] = (TextView) findViewById(R.id.RO073);

        DisplayBoardBackgroundSelected[0][4] = (TextView) findViewById(R.id.RO004);
        DisplayBoardBackgroundSelected[1][4] = (TextView) findViewById(R.id.RO014);
        DisplayBoardBackgroundSelected[2][4] = (TextView) findViewById(R.id.RO024);
        DisplayBoardBackgroundSelected[3][4] = (TextView) findViewById(R.id.RO034);
        DisplayBoardBackgroundSelected[4][4] = (TextView) findViewById(R.id.RO044);
        DisplayBoardBackgroundSelected[5][4] = (TextView) findViewById(R.id.RO054);
        DisplayBoardBackgroundSelected[6][4] = (TextView) findViewById(R.id.RO064);
        DisplayBoardBackgroundSelected[7][4] = (TextView) findViewById(R.id.RO074);

        DisplayBoardBackgroundSelected[0][5] = (TextView) findViewById(R.id.RO005);
        DisplayBoardBackgroundSelected[1][5] = (TextView) findViewById(R.id.RO015);
        DisplayBoardBackgroundSelected[2][5] = (TextView) findViewById(R.id.RO025);
        DisplayBoardBackgroundSelected[3][5] = (TextView) findViewById(R.id.RO035);
        DisplayBoardBackgroundSelected[4][5] = (TextView) findViewById(R.id.RO045);
        DisplayBoardBackgroundSelected[5][5] = (TextView) findViewById(R.id.RO055);
        DisplayBoardBackgroundSelected[6][5] = (TextView) findViewById(R.id.RO065);
        DisplayBoardBackgroundSelected[7][5] = (TextView) findViewById(R.id.RO075);

        DisplayBoardBackgroundSelected[0][6] = (TextView) findViewById(R.id.RO006);
        DisplayBoardBackgroundSelected[1][6] = (TextView) findViewById(R.id.RO016);
        DisplayBoardBackgroundSelected[2][6] = (TextView) findViewById(R.id.RO026);
        DisplayBoardBackgroundSelected[3][6] = (TextView) findViewById(R.id.RO036);
        DisplayBoardBackgroundSelected[4][6] = (TextView) findViewById(R.id.RO046);
        DisplayBoardBackgroundSelected[5][6] = (TextView) findViewById(R.id.RO056);
        DisplayBoardBackgroundSelected[6][6] = (TextView) findViewById(R.id.RO066);
        DisplayBoardBackgroundSelected[7][6] = (TextView) findViewById(R.id.RO076);

        DisplayBoardBackgroundSelected[0][7] = (TextView) findViewById(R.id.RO007);
        DisplayBoardBackgroundSelected[1][7] = (TextView) findViewById(R.id.RO017);
        DisplayBoardBackgroundSelected[2][7] = (TextView) findViewById(R.id.RO027);
        DisplayBoardBackgroundSelected[3][7] = (TextView) findViewById(R.id.RO037);
        DisplayBoardBackgroundSelected[4][7] = (TextView) findViewById(R.id.RO047);
        DisplayBoardBackgroundSelected[5][7] = (TextView) findViewById(R.id.RO057);
        DisplayBoardBackgroundSelected[6][7] = (TextView) findViewById(R.id.RO067);
        DisplayBoardBackgroundSelected[7][7] = (TextView) findViewById(R.id.RO077);

    }


    @Override
    public void onClick(View v) {
        // Assign the Square clicked by the user to click

        switch (v.getId()) {
            case R.id.R00:
                click = Square.A1;
                break;
            case R.id.R10:
                click = Square.B1;
                break;
            case R.id.R20:
                click = Square.C1;
                break;
            case R.id.R30:
                click = Square.D1;
                break;
            case R.id.R40:
                click = Square.E1;
                break;
            case R.id.R50:
                click = Square.F1;
                break;
            case R.id.R60:
                click = Square.G1;
                break;
            case R.id.R70:
                click = Square.H1;
                break;

            case R.id.R01:
                click = Square.A2;
                break;
            case R.id.R11:
                click = Square.B2;
                break;
            case R.id.R21:
                click = Square.C2;
                break;
            case R.id.R31:
                click = Square.D2;
                break;
            case R.id.R41:
                click = Square.E2;
                break;
            case R.id.R51:
                click = Square.F2;
                break;
            case R.id.R61:
                click = Square.G2;
                break;
            case R.id.R71:
                click = Square.H2;
                break;

            case R.id.R02:
                click = Square.A3;
                break;
            case R.id.R12:
                click = Square.B3;
                break;
            case R.id.R22:
                click = Square.C3;
                break;
            case R.id.R32:
                click = Square.D3;
                break;
            case R.id.R42:
                click = Square.E3;
                break;
            case R.id.R52:
                click = Square.F3;
                break;
            case R.id.R62:
                click = Square.G3;
                break;
            case R.id.R72:
                click = Square.H3;
                break;

            case R.id.R03:
                click = Square.A4;
                break;
            case R.id.R13:
                click = Square.B4;
                break;
            case R.id.R23:
                click = Square.C4;
                break;
            case R.id.R33:
                click = Square.D4;
                break;
            case R.id.R43:
                click = Square.E4;
                break;
            case R.id.R53:
                click = Square.F4;
                break;
            case R.id.R63:
                click = Square.G4;
                break;
            case R.id.R73:
                click = Square.H4;
                break;

            case R.id.R04:
                click = Square.A5;
                break;
            case R.id.R14:
                click = Square.B5;
                break;
            case R.id.R24:
                click = Square.C5;
                break;
            case R.id.R34:
                click = Square.D5;
                break;
            case R.id.R44:
                click = Square.E5;
                break;
            case R.id.R54:
                click = Square.F5;
                break;
            case R.id.R64:
                click = Square.G5;
                break;
            case R.id.R74:
                click = Square.H5;
                break;

            case R.id.R05:
                click = Square.A6;
                break;
            case R.id.R15:
                click = Square.B6;
                break;
            case R.id.R25:
                click = Square.C6;
                break;
            case R.id.R35:
                click = Square.D6;
                break;
            case R.id.R45:
                click = Square.E6;
                break;
            case R.id.R55:
                click = Square.F6;
                break;
            case R.id.R65:
                click = Square.G6;
                break;
            case R.id.R75:
                click = Square.H6;
                break;

            case R.id.R06:
                click = Square.A7;
                break;
            case R.id.R16:
                click = Square.B7;
                break;
            case R.id.R26:
                click = Square.C7;
                break;
            case R.id.R36:
                click = Square.D7;
                break;
            case R.id.R46:
                click = Square.E7;
                break;
            case R.id.R56:
                click = Square.F7;
                break;
            case R.id.R66:
                click = Square.G7;
                break;
            case R.id.R76:
                click = Square.H7;
                break;

            case R.id.R07:
                click = Square.A8;
                break;
            case R.id.R17:
                click = Square.B8;
                break;
            case R.id.R27:
                click = Square.C8;
                break;
            case R.id.R37:
                click = Square.D8;
                break;
            case R.id.R47:
                click = Square.E8;
                break;
            case R.id.R57:
                click = Square.F8;
            case R.id.R67:
                click = Square.G8;
                break;
            case R.id.R77:
                click = Square.H8;
                break;
        }


        ArrayList<Move> allowMoves = new ArrayList<>();

        if(ismyturn){
            if (c1==null) {
                c1=click;
                colorMove(c1);

            } else {
                if (c1!=null && c2==null) {
                    c2=click;
                    Move mo=new Move(c1,c2);
                    if(isaMove(mo)){
                        if(board.getPiece(mo.getFrom())== Piece.WHITE_PAWN && (mo.getTo()==Square.A8 || mo.getTo()==Square.B8 || mo.getTo()==Square.C8 || mo.getTo()==Square.D8 || mo.getTo()==Square.E8 || mo.getTo()==Square.F8 || mo.getTo()==Square.G8 || mo.getTo()==Square.H8)){
                            currentTask=10;
                            pawn_choices.setVisibility(View.VISIBLE);
                            imlistenig.setVisibility(View.VISIBLE);
                            return;
                        }else {
                            if(board.getPiece(mo.getFrom())== Piece.BLACK_PAWN && (mo.getTo()==Square.A1 || mo.getTo()==Square.B1 || mo.getTo()==Square.C1 || mo.getTo()==Square.D1 || mo.getTo()==Square.E1 || mo.getTo()==Square.F1 || mo.getTo()==Square.G1 || mo.getTo()==Square.H1)){
                                pawn_choices.setVisibility(View.VISIBLE);
                                imlistenig.setVisibility(View.VISIBLE);
                                currentTask=10;
                                return;
                            }
                        }
                        board.doMove(mo);
                        clearBoardColor();
                        moveBoard(parseBoard());

                        if (board.isKingAttacked()){
                            colorRedking(parseBoard());
                        }
                        if (board.isMated() || board.isDraw() || board.isStaleMate() || board.isInsufficientMaterial() || board.isRepetition()){

                            game_over.setVisibility(View.VISIBLE);
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }

                    /*
                    ------------------------------------------------
                    COMPUTER -> GIOCATORE AVVERSARIO
                    ------------------------------------------------


                     */

                        board.doMove(player.eseguiMossa(board.legalMoves()));
                        ismyturn=false;
                        CountDownTimer timer=new CountDownTimer(2000,1000) {
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                moveBoard(parseBoard());
                                clearBoardColor();
                                if (board.isKingAttacked()){
                                    colorRedking(parseBoard());
                                }
                                ismyturn=true;
                                currentTask=0;
                                currentStep=0;
                                returnedText.setText(suggestions.getFirstMessage());
                                imlistenig.setVisibility(View.GONE);
                            }
                        }.start();
                    /*
                    ---------------------FINE-----------------------
                    COMPUTER -> GIOCATORE AVVERSARIO
                    ------------------------------------------------
                     */

                        if (board.isMated() || board.isDraw() || board.isStaleMate() || board.isInsufficientMaterial() || board.isRepetition()){
                            //System.out.println("scacco matto or Draw or stallo");
                            game_over.setVisibility(View.VISIBLE);
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                        clearDuble();
                    }else {
                        clearBoardColor();
                        c1=c2;
                        c2=null;
                        c1=click;
                        colorMove(c1);
                    }
                }
            }
            if (board.isMated() || board.isDraw() || board.isStaleMate() || board.isInsufficientMaterial() || board.isRepetition()){
                //System.out.println("scacco matto or Draw or stallo");
                game_over.setVisibility(View.VISIBLE);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }



    }



    public void pawnChoice (View view){
        pawn_choices.setVisibility(View.INVISIBLE);
        imlistenig.setVisibility(View.INVISIBLE);
        returnedText.setText("Please chose among:\n'Regina'\n'Alfiere'\n'Torre'\n'Cavallo'");
        TextView t= (TextView) view;
        switch (t.getText().toString()){
            case "Queen":   lastChoice = (board.getSideToMove().equals(Side.WHITE)) ?  Piece.WHITE_QUEEN :  Piece.BLACK_QUEEN;
                            break;
            case "Bishop":   lastChoice = (board.getSideToMove().equals(Side.WHITE)) ?  Piece.WHITE_BISHOP :  Piece.BLACK_BISHOP;
                            break;
            case "Rock":   lastChoice = (board.getSideToMove().equals(Side.WHITE)) ?  Piece.WHITE_ROOK:  Piece.BLACK_ROOK;
                            break;
            case "Knight":   lastChoice = (board.getSideToMove().equals(Side.WHITE)) ?  Piece.WHITE_KNIGHT :  Piece.BLACK_KNIGHT;
                            break;
        }

        board.doMove(new Move(c1,c2,lastChoice));
        clearBoardColor();
        moveBoard(parseBoard());
        currentTask=0;
        currentStep=0;
        returnedText.setText(suggestions.getFirstMessage());
        imlistenig.setVisibility(View.GONE);
        /*
         ------------------------------------------------
         COMPUTER -> GIOCATORE AVVERSARIO
         ------------------------------------------------
         NOTE: try and catch per lo sleep
        */

        board.doMove(player.eseguiMossa(board.legalMoves()));
        CountDownTimer timer=new CountDownTimer(2000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished==2000){
                    ismyturn=false;
                }

            }

            @Override
            public void onFinish() {
                moveBoard(parseBoard());
                ismyturn=true;
                currentTask=0;
                currentStep=0;
                returnedText.setText(suggestions.getFirstMessage());
                imlistenig.setVisibility(View.GONE);
            }
        }.start();
         /*
         ---------------------FINE-----------------------
         COMPUTER -> GIOCATORE AVVERSARIO
         ------------------------------------------------
         */
        //System.out.println(board.toString());
        clearDuble();
    }


    private boolean isaMove(Move mo) {

        for (Move m : board.legalMoves()) {
            if (m.equals(mo) || m.toString().contains(mo.toString()))
                return true;
        }
        return false;
    }


    private  Move searchMove(String mosse) {
        mosse= mosse.toUpperCase(Locale.ROOT);
        String[] pos=mosse.split(" ");
        Square a=null;
        Square b=null;
        try {
            a= Square.fromValue(pos[0]);
            b= Square.fromValue(pos[1]);
        }catch (IllegalArgumentException e){
            return null;
        }
        for (Move m : board.legalMoves()) {
            if ((m.getTo().equals(a) && m.getFrom().equals(b)) || (m.getTo().equals(b) && m.getFrom().equals(a)) )
                return m;
        }
        return null;
    }

    /**
     * This function parse the board and create a matrix that return
     * @return return a matrix represented the board
     */
    private String [][] parseBoard(){
        String str=board.toString();

        String[] array = str.split("", -1);
        ArrayList<String> pos= new ArrayList<>();
        boolean cont=true;
        for(int i=0; i<array.length;i++){
            if(cont){
                if (array[i].equals("S")){
                    cont=false;
                }else{
                    if (!array[i].equals("\n")){
                        pos.add(array[i]);
                    }
                }
            }
        }

        List<String> r1=  pos.subList(1,9);
        List<String> r2=  pos.subList(9,17);
        List<String> r3=  pos.subList(17,25);
        List<String> r4=  pos.subList(25,33);
        List<String> r5=  pos.subList(33,41);
        List<String> r6=  pos.subList(41,49);
        List<String> r7=  pos.subList(49,57);
        List<String> r8=  pos.subList(57,65);


        String [][] matrix=new String[8][8];

        matrix [0]= r8.toArray(new String[0]);
        matrix [1]= r7.toArray(new String[0]);
        matrix [2]= r6.toArray(new String[0]);
        matrix [3]= r5.toArray(new String[0]);
        matrix [4]= r4.toArray(new String[0]);
        matrix [5]= r3.toArray(new String[0]);
        matrix [6]= r2.toArray(new String[0]);
        matrix [7]= r1.toArray(new String[0]);

        return matrix;
    }

    /**
     *
     *
     * Remark: Java is Row first, but the display JAVA è ROW FIRST MA IL DISPLAY BOARD E TUTTA L'ACTIVITY è STATA INIZIALIZZATA AL CONTRARIO
     */
    private void moveBoard(String[][] matrix){
        for (int row=0;row<8;row++){
            for (int col=0; col<8; col++){
                switch (matrix[row][col]){
                    case "r":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.brook);
                        break;
                    case "n":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.bknight);
                        break;
                    case "b":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.bbishop);
                        break;
                    case "q":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.bqueen);
                        break;
                    case "k":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.bking);
                        break;
                    case "p":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.bpawn);
                        break;
                    case "R":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.wrook);
                        break;
                    case "N":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.wknight);
                        break;
                    case "B":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.wbishop);
                        break;
                    case "Q":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.wqueen);
                        break;
                    case "K":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.wking);
                        break;
                    case "P":
                        DisplayBoard[col][row].setBackgroundResource(R.drawable.wpawn);
                        break;
                    case ".":
                        DisplayBoard[col][row].setBackgroundResource(0);
                        break;

                }
            }
        }
    }

    /**
     * @param pos Final square of the moves
     * @return List of coordinates
     */
    private Integer parseYAssis (int pos){

        HashMap<Integer, Integer> Y = new HashMap<Integer, Integer>() {{
            put(0,7);
            put(1,6);
            put(2,5);
            put(3,4);
            put(4,3);
            put(5,2);
            put(6,1);
            put(7,0);
        }};

       return Y.get(pos);
    }





    /**
     * Clear all color of a boards
     */
    @SuppressLint("ResourceAsColor")
    private void clearBoardColor(){
        List<TextView> dark= new LinkedList<TextView>(){{
           add((TextView) findViewById(R.id.RO000));
           add((TextView) findViewById(R.id.RO002));
           add((TextView) findViewById(R.id.RO004));
           add((TextView) findViewById(R.id.RO006));

           add((TextView) findViewById(R.id.RO017));
           add((TextView) findViewById(R.id.RO015));
           add((TextView) findViewById(R.id.RO013));
           add((TextView) findViewById(R.id.RO011));

           add((TextView) findViewById(R.id.RO020));
           add((TextView) findViewById(R.id.RO022));
           add((TextView) findViewById(R.id.RO024));
           add((TextView) findViewById(R.id.RO026));

            add((TextView) findViewById(R.id.RO037));
            add((TextView) findViewById(R.id.RO035));
            add((TextView) findViewById(R.id.RO033));
            add((TextView) findViewById(R.id.RO031));

            add((TextView) findViewById(R.id.RO040));
            add((TextView) findViewById(R.id.RO042));
            add((TextView) findViewById(R.id.RO044));
            add((TextView) findViewById(R.id.RO046));

            add((TextView) findViewById(R.id.RO057));
            add((TextView) findViewById(R.id.RO055));
            add((TextView) findViewById(R.id.RO053));
            add((TextView) findViewById(R.id.RO051));

            add((TextView) findViewById(R.id.RO060));
            add((TextView) findViewById(R.id.RO062));
            add((TextView) findViewById(R.id.RO064));
            add((TextView) findViewById(R.id.RO066));

            add((TextView) findViewById(R.id.RO077));
            add((TextView) findViewById(R.id.RO075));
            add((TextView) findViewById(R.id.RO073));
            add((TextView) findViewById(R.id.RO071));
        }};


        for (TextView[] d : DisplayBoardBackgroundSelected){
            for(TextView t:d){
                if(dark.contains(t)){
                    t.setBackgroundColor(Color.parseColor("#0586c8"));
                }else {
                    t.setBackgroundColor(Color.parseColor("#ffffff"));
                }
            }
        }


    }



    /**
     * Reset clicks
     */
    public void clearDuble(){
        c1=null;
        c2=null;
    }


    /**
     *This function color the squares in which it is possible to move
     * @param s Init moves square
     */
    @SuppressLint({"ResourceAsColor", "Range"})
    private void colorMove (Square s) {
        for (Move m : board.legalMoves()) {
            if (m.getFrom() == s) {
                DisplayBoardBackgroundSelected[parseMove(m.getTo()).get(0)][parseMove(m.getTo()).get(1)].setBackgroundColor(Color.parseColor("#A67FFFD4"));

            }
        }
    }

    /**
     * This function color the king position if is under attak
     */
    private void colorRedking(String[][] matrix){

        String king;
        if (board.getSideToMove()== Side.WHITE){
            king="K";
        }else {
            king="k";
        }

        for (int row=0;row<8;row++){
            for (int col=0; col<8; col++){
                if (matrix[row][col].equals(king))
                    DisplayBoardBackgroundSelected[col][row].setBackgroundColor(Color.parseColor("#A6fc0703"));
            }
        }
    }

    /**
     * @param pos Final square of the moves
     * @return List of coordinates
     */
    private List<Integer> parseMove (Square pos){

        HashMap<String, Integer> X = new HashMap<String, Integer>() {{
            put("A", 0);
            put("B", 1);
            put("C", 2);
            put("D", 3);
            put("E", 4);
            put("F", 5);
            put("G", 6);
            put("H", 7);
        }};

        HashMap<String, Integer> Y = new HashMap<String, Integer>() {{
            put("1", 0);
            put("2", 1);
            put("3", 2);
            put("4", 3);
            put("5", 4);
            put("6", 5);
            put("7", 6);
            put("8", 7);
        }};

        String posS = (String) pos.name();
        String col = posS.substring(0, 1);
        String row = posS.substring(1, 2);

        LinkedList<Integer> ret = new LinkedList<>();
        ret.add(X.get(col));
        ret.add(Y.get(row));
        return ret;
    }
    /**
     * giving s cell return all moves
     */
    private List<Move> cellMoves(Square s){
        List<Move> moveList= new LinkedList<>();
        for (Move mo: board.legalMoves()){
            if (mo.getFrom().equals(s)){
                moveList.add(mo);
            }
        }
        return moveList;
    }
    public void bestmove(View view) {
        Move mo=player.eseguiMossa(board.legalMoves());
        List<Integer> coordinate_from=parseMove(mo.getFrom());
        String coordinate_f="R"+coordinate_from.get(0) +""+coordinate_from.get(1);
        View from=findViewById(getResources().getIdentifier(coordinate_f,"id", getBaseContext().getPackageName()));
        onClick(from);
        List<Integer> coordinate_to=parseMove(mo.getTo());
        String coordinate_t="R"+coordinate_to.get(0) +""+coordinate_to.get(1);
        View to=findViewById(getResources().getIdentifier(coordinate_t,"id", getBaseContext().getPackageName()));
        onClick(to);
    }

    public void settings(View view) {
        if (settingsMenu.getVisibility() != View.VISIBLE){
            settingsMenu.setVisibility(View.VISIBLE);
        }else {
            settingsMenu.setVisibility(View.GONE);
            currentTask=0;
            currentStep=0;
            returnedText.setText(suggestions.getFirstMessage());
            imlistenig.setVisibility(View.GONE);
        }

    }

    public void cast(View view) {
        System.out.println("cast");
        startActivity(new Intent("android.settings.CAST_SETTINGS"));
    }

    public void restart(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    public void exit(View view){
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void speak(String sentence){
        tt.speak(sentence, TextToSpeech.QUEUE_FLUSH,null,null);
    }

    public void Back(View view) {
        settingsMenu.setVisibility(View.GONE);
        currentTask=0;
        currentStep=0;
        returnedText.setText(suggestions.getFirstMessage());
        imlistenig.setVisibility(View.GONE);
    }
}