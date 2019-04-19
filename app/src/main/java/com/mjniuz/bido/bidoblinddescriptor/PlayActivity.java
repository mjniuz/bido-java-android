package com.mjniuz.bido.bidoblinddescriptor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class PlayActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, SurfaceHolder.Callback {
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
    boolean flag = false;
    boolean flag2 = false;
    boolean flagDesc = false;
    boolean flagDesc2 = false;


    private PopupWindow mPopupWindow;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            if (mBluetoothAdapter.isEnabled() == false) {
                mBluetoothAdapter.enable();
            }
        }

        /*try {
            adjustBright();
        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        if(!isNetworkAvailable()){
            refresh();
        }else{
            playNotify("success.wav");
            // init speaking
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS) {
                        Locale locale = new Locale("id", "ID");
                        tts.setLanguage(locale);

                        Bundle b = getIntent().getExtras();
                        int value = -1; // or other values
                        if(b != null)
                            value = b.getInt("redirected");

                        String msg  = "Alat sudah siap";
                        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                        toast.show();
                        speak(msg);
                    }else{
                        refresh();
                    }
                }
            });
        }

        /*Button replyBtn = (Button) findViewById(R.id.buttonAlert);
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

        });*/

        camera_image = (ImageView) findViewById(R.id.camera_image); //NEEDED FOR THE PREVIEW
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


                new Handler().postDelayed(
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
                /*if(!isNetworkAvailable()){
                    refresh();
                }

                callApi("reply", "");
                playNotify("beep-ok.wav");
                Log.d("FASTCLICK", "YA");*/


                // play soound
                playNotify("beep.wav");

                final String outputFile   = getFilename();


                new Handler().postDelayed(
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
            }
        });

        /*Button testBtn = (Button) findViewById(R.id.buttonTest);
        testBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    refresh();
                }
                mCamera.takePicture(shutterCallback,rawCallback,testCallback);
            }
        });*/
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.i("key pressed", String.valueOf(event.getKeyCode()));
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == 99) {
            // record audio
            Log.d("Test", "Long press!");
            playNotify("beep.wav");
            final String outputFile   = getFilename();

            new Handler().postDelayed(
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
            flag = false;
            flag2 = true;

            return true;
        }

        if (keyCode == 97) {
            flagDesc = false;
            flagDesc2 = true;

            refresh();
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("PRESS", "BACKPRESS");
        return;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!isNetworkAvailable()){
            refresh();
            return true;
        }

        if(keyCode == 97){
            // descriptor
            event.startTracking();
            if (flagDesc2 == true) {
                flagDesc = false;
            } else {
                flagDesc = true;
                flagDesc2 = false;
            }

            return true;
        }

        if(keyCode == 100){
            // face detection
            mCamera.takePicture(shutterCallback,rawCallback,faceCallback);
            return true;
        }

        if(keyCode == 96){
            // text
            mCamera.takePicture(shutterCallback,rawCallback,textCallback);
            return true;
        }

        // disable all button except volume
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == event.KEYCODE_VOLUME_UP){
            Log.d("PRESS", "Volume");
            return super.onKeyDown(keyCode, event);
        }

        if(keyCode == 99){
            // reply
            // just reply without audio record
            event.startTracking();
            if (flag2 == true) {
                flag = false;
            } else {
                flag = true;
                flag2 = false;
            }

            return true;
        }

        return false; //super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 99) {
            event.startTracking();
            if (flag) {
                Log.d("Test", "Short");
                // reply
                callApi("reply", "");
                playNotify("beep-ok.wav");
            }
            flag = true;
            flag2 = false;
            return true;
        }

        if (keyCode == 97) {
            event.startTracking();
            if (flagDesc) {
                mCamera.takePicture(shutterCallback,rawCallback,descCallback);
            }
            flagDesc = true;
            flagDesc2 = false;
            return true;
        }

        return super.onKeyUp(keyCode, event);
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

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        try {
                            stop(outputFile);
                            Log.d("AUDIO", outputFile.toString());

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

        //playNotify("beep-error.wav");

        Intent redirect = new Intent(PlayActivity.this, RedirectActivity.class);
        Bundle b = new Bundle();
        b.putInt("wait", 10000); //Your id
        redirect.putExtras(b); //Put your id to your next Intent
        startActivity(redirect);

        PlayActivity.this.startActivity(redirect);

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
        if(!isNetworkAvailable()){
            return "";
        }

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
        if (status == TextToSpeech.ERROR) {
            refresh();
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

        //stopPreviewAndFreeCamera();
        /*try {
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopPreviewAndFreeCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            Camera.Parameters params = mCamera.getParameters();
            int widthInt = params.getPictureSize().width;
            int heightInt = params.getPictureSize().height;
            Log.d("CAM_WIDTH", String.valueOf(widthInt));
            Log.d("CAM_HEIGHT", String.valueOf(heightInt));

            //params.setPictureSize(defWidth, defHeight);
            //params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();


            Intent i = new Intent(PlayActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if(b != null)
            value = b.getInt("redirected");

        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            Camera.Size selected = sizes.get(0);
            params.setPreviewSize(selected.width,selected.height);
            mCamera.setParameters(params);

            //int rotate = getCorrectCameraOrientation(info,mCamera);
            //Log.d("rotate", String.valueOf(rotate));
            //setDisplayOrientation(mCamera, rotate);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCorrectCameraOrientation(Camera.CameraInfo info, Camera camera) {
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch(rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;

        }

        int result;
        if(info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees + 90) % 360;
            result = (360 - result) % 360;
        }else{
            result = (info.orientation - degrees + 360 + 90) % 360;
        }

        return result;
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


    private void adjustBright() throws Settings.SettingNotFoundException {
        // TODO Auto-generated method stub
        int brightnessMode = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE);
        if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = 0F;
        getWindow().setAttributes(layoutParams);
    }
}
