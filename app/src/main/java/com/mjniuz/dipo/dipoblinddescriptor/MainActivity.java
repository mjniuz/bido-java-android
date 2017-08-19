package com.mjniuz.dipo.dipoblinddescriptor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, SurfaceHolder.Callback {
    final Context context = this;
    private Button button;
    private TextToSpeech tts;
    SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    ImageView imageView;
    private static final int CAMERA_REQUEST = 1888;
    private ImageView mImage;
    private ImageView camera_image;
    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;
    SurfaceView preview;
    MediaRecorder recorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        // init speaking
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Locale locale = new Locale("id", "ID");
                    tts.setLanguage(locale);


                    Bundle b = getIntent().getExtras();
                    int value = -1; // or other values
                    if(b != null)
                        value = b.getInt("redirected");

                    if(!isNetworkAvailable()){
                        refresh();
                    }else if(value == -1){
                        String msg  = "Alat sudah siap";
                        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                        toast.show();
                        speak(msg);
                    }
                }
            }
        });

        Button replyBtn = (Button) findViewById(R.id.buttonAlert);
        replyBtn.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if(!isNetworkAvailable()){
                        refresh();
                        return false;
                    }

                    callApi("reply", "");
                    // Do what you want
                    return true;
                }
                return false;
            }

        });


        camera_image = (ImageView) findViewById(R.id.camera_image);//NEEDED FOR THE PREVIEW
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCamera = Camera.open();

        Button descriptorBtn = (Button) findViewById(R.id.descriptorBtn);
        descriptorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    refresh();
                }
                mCamera.takePicture(shutterCallback,rawCallback,descCallback);
            }
        });

        Button faceBtn = (Button) findViewById(R.id.facBtn);
        faceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    refresh();
                }
                mCamera.takePicture(shutterCallback,rawCallback,faceCallback);
            }
        });

        Button textBtn = (Button) findViewById(R.id.buttonText);
        textBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    refresh();
                }
                mCamera.takePicture(shutterCallback,rawCallback,textCallback);
            }
        });

        final Button audioBtn = (Button) findViewById(R.id.audioBtn);
        audioBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // play soound
                playNotify("beep.wav");

                final String outputFile   = getFilename();


                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                try {
                                    startRecording(outputFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        100);
                Log.d("LONGCLICK", outputFile);
                return true;
            }

        });

        audioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    refresh();
                }

                callApi("reply", "");
                playNotify("beep-ok.wav");
                Log.d("FASTCLICK", "YA");
            }
        });

        Button testBtn = (Button) findViewById(R.id.buttonTest);
        testBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    refresh();
                }
                mCamera.takePicture(shutterCallback,rawCallback,testCallback);
            }
        });
    }

    public String startRecording(final String outputFile) throws IOException{
        recorder            = new MediaRecorder();
        int output_formats  = MediaRecorder.OutputFormat.AAC_ADTS;

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(outputFile);
        recorder.setAudioSamplingRate(44100);  // 44.1 khz
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);
        recorder.setMaxDuration(3000);

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        recorder.start();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        try {
                            stop(outputFile);

                            // play soound
                            playNotify("beep-ok.wav");
                            callApi("reply", outputFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                3000);
        return null;
    }

    public void playNotify(String soundName){
        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd(soundName);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop(String outputFile) throws IOException {
        recorder.stop();
        recorder.reset();
        recorder.release();

        Log.d("RECORDSTOP", outputFile);
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    public String getFilename(){
        String AUDIO_RECORDER_FOLDER = "/DCIM/DIPO";
        String file_exts[] = { ".aac", ".aac" };
        int currentFormat = 0;
        String AUDIO_NAME  = "audio-temp";
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + AUDIO_NAME + file_exts[currentFormat]);
    }

    public void refresh(){
        String msg  = "Koneksi Internet bermasalah, Harap periksa koneksi atau silahkan tunggu beberapa saat untuk inisialisasi";
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
        speak(msg);

        Intent redirect = new Intent(MainActivity.this, RedirectActivity.class);
        Bundle b = new Bundle();
        b.putInt("wait", 4000); //Your id
        redirect.putExtras(b); //Put your id to your next Intent
        startActivity(redirect);
        finish();
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };


    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback testCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            captureCam(data, "test-post");
        }
    };

    Camera.PictureCallback textCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            captureCam(data, "text");
        }
    };

    Camera.PictureCallback faceCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            captureCam(data, "face");
        }
    };

    Camera.PictureCallback descCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            captureCam(data, "descriptor");
        }
    };

    public void captureCam(byte[] data, String type){
        String cameraPath = null;
        try {
            cameraPath = new CameraPreview(data).execute().get();

            callApi(type, cameraPath);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        resetCam();
    }

    public void callApi(String type, String filePaths){
        String apiType; // default
        switch (type){
            case "face":
                apiType = "face";
                break;
            case "text":
                apiType = "text";
                break;
            case "reply":
                apiType = "reply";
                break;
            case "test-post":
                apiType = "test-post";
                break;
            case "test-post-audio":
                apiType = "test-post-audio";
                break;
            default:
                apiType = "descriptor";
        }

        //String requestURL = "https://dipo.mjniuz.com/api/" + apiType;

        String response = null;

        String[] arrayw = new String[2]; //populate array
        arrayw[0]    = apiType;
        arrayw[1]    = filePaths;
        try {
            response = new HttpRequest().execute(arrayw).get();
            if(apiType != "test-post" && apiType != "test-post-audio"){
                String msg  = getMessage(response);
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();

                speak(msg);
            }else{
                Toast toast = Toast.makeText(context, response, Toast.LENGTH_LONG);
                toast.show();
            }

            Log.d("RESULT", response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    private void resetCam() {
        mCamera.startPreview();
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("LANG", String.valueOf(tts.getAvailableLanguages() ));
            tts.stop();
            tts.speak(text,TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    private void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    private void startPreviewAndFreeCamera() {
        if (mCamera == null) {
            camera_image = (ImageView) findViewById(R.id.camera_image);//NEEDED FOR THE PREVIEW
            mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
            mSurfaceView.getHolder().addCallback(this);
            mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCamera = Camera.open();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getMessage(String str){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String msg = "";
        try {
            msg = jsonObject.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return msg;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(tts != null){
            tts.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCamera.stopPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureSize(1920, 1080);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if(b != null)
            value = b.getInt("redirected");

        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        params.setPreviewSize(selected.width,selected.height);
        mCamera.setParameters(params);

        setDisplayOrientation(mCamera, value);
        mCamera.startPreview();
    }

    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
