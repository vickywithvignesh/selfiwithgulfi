package com.twilio.video.quickstart.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.icu.text.IDNA;
import android.media.AudioManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.twilio.video.CameraParameterUpdater;
import com.twilio.video.LocalParticipant;
import com.twilio.video.RoomState;
import com.twilio.video.Video;
import com.twilio.video.VideoRenderer;
import com.twilio.video.TwilioException;
import com.twilio.video.quickstart.R;
import com.twilio.video.quickstart.dialog.Dialog;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.CameraCapturer.CameraSource;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import static android.R.attr.bitmap;
import static android.R.attr.value;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.twilio.video.quickstart.R.id.res;
import static com.twilio.video.quickstart.R.id.video_container;

public class VideoActivity extends AppCompatActivity {
    Button b1;
    TextView t1;
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "VideoActivity";
String URL="http://10.0.1.9:3000/tokens";
    /*
     * You must provide a Twilio Access Token to connect to the Video service
     *
     */
    //String tokenfromserver="http://localhost:3000/tokens".getBytes(null);
   // private static final String TWILIO_ACCESS_TOKEN ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzcyOGI2YjdkNDBiMzZmMmZkZWY1NTI0NDdkNTRlMDlkLTE0OTYxNzUwMTYiLCJpc3MiOiJTSzcyOGI2YjdkNDBiMzZmMmZkZWY1NTI0NDdkNTRlMDlkIiwic3ViIjoiQUNlNjhkYzE5YTVjZWE0ODAwMWM5OTgyMzgwZGM5ZDFmMyIsImV4cCI6MTQ5NjE3ODYxNiwiZ3JhbnRzIjp7ImlkZW50aXR5IjoiZ2hnaCIsInZpZGVvIjp7InJvb20iOiJteXJvb20ifX19.gCA7-Br5DqGJ0zJwW0Hf1g9iaDCkj8Dj2cvu4AGOw7E";


    private String TWILIO_ACCESS_TOKEN;
    // String TWILIO_ACCESS_TOKEN;
    /* Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */
    private String accessToken;

    /*

     * A Room represents communication between a local participant and one or more participants.
     */
    private Room room;
    private LocalParticipant localParticipant;

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private VideoView primaryVideoView;
    private VideoView thumbnailVideoView;


    /*
     * Android application UI elements
     */
    private TextView videoStatusTextView;
    private CameraCapturer cameraCapturer;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private FloatingActionButton connectActionFab;
    private FloatingActionButton switchCameraActionFab;
    private FloatingActionButton localVideoActionFab;
    private FloatingActionButton muteActionFab;
    private android.support.v7.app.AlertDialog alertDialog;
    private AudioManager audioManager;
    private String participantIdentity;

    private int previousAudioMode;
    private boolean previousMicrophoneMute;
    private VideoRenderer localVideoView;
    private boolean disconnectedFromOnDestroy;
    ImageView imageView;
    Bitmap mbitmap;
    Button captureScreenShot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log.d("fdf",tokenfromserver);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        View rootView = getWindow().getDecorView().findViewById(R.id.primary_video_view);
    b1 = (Button) findViewById(R.id.res);
        t1 = (TextView) findViewById(R.id.textView);
        primaryVideoView = (VideoView) findViewById(R.id.primary_video_view);
        thumbnailVideoView = (VideoView) findViewById(R.id.thumbnail_video_view);
        videoStatusTextView = (TextView) findViewById(R.id.video_status_textview);

        connectActionFab = (FloatingActionButton) findViewById(R.id.connect_action_fab);
        switchCameraActionFab = (FloatingActionButton) findViewById(R.id.switch_camera_action_fab);
        localVideoActionFab = (FloatingActionButton) findViewById(R.id.local_video_action_fab);
        muteActionFab = (FloatingActionButton) findViewById(R.id.mute_action_fab);

      b1.setOnClickListener(new View.OnClickListener() {
          @Override
            public void onClick(View v) {
                final RequestQueue requestQueue = Volley.newRequestQueue(VideoActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                        new Response.Listener<String>() {
                            public void onResponse(String response)
                            {
//t1.setText(response);
                            TWILIO_ACCESS_TOKEN=response;
                                Toast.makeText(getApplicationContext(),"TOKEN GENERATED",Toast.LENGTH_SHORT).show();
                                //createAudioAndVideoTracks();
                          setAccessToken();
                              //  Log.i("TWILIO",TWILIO_ACCESS_TOKEN);
                                requestQueue.stop();
                            }

                        }, new Response.ErrorListener(){
                public void onErrorResponse (VolleyError Volley) {
                    Toast.makeText(getApplicationContext(),"TRY YOUR INTERNET CONNECTION",Toast.LENGTH_SHORT).show();
t1.setText("some thing went wrong");

                    System.out.println(Volley);
                    requestQueue.stop();
                } });
                requestQueue.add(stringRequest);
            }

        });

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        /*
         * Check camera and microphone permissions. Needed in Android M.
         */
        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        } else {
            createAudioAndVideoTracks();
        //  setAccessToken();
        }

        /*
         * Set the initial state of the UI
         */
        intializeUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            boolean cameraAndMicPermissionGranted = true;

            for (int grantResult : grantResults) {
                cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks();
                setAccessToken();
            } else {
                Toast.makeText(this,
                        R.string.permissions_needed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();
        /*
         * If the local video track was released when the app was put in the background, recreate.
         */
        if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer);
            localVideoTrack.addRenderer(localVideoView);

            /*
             * If connected to a Room then share the local video track.
             */
            if (localParticipant != null) {
                localParticipant.addVideoTrack(localVideoTrack);
            }
        }
    }

    @Override
    protected void onPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null) {
            /*
             * If this local video track is being shared in a Room, remove from local
             * participant before releasing the video track. Participants will be notified that
             * the track has been removed.
             */
            if (localParticipant != null) {
                localParticipant.removeVideoTrack(localVideoTrack);
            }

            localVideoTrack.release();
            localVideoTrack = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != RoomState.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }

        super.onDestroy();
    }

    private boolean checkPermissionForCameraAndMicrophone(){
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultCamera == PackageManager.PERMISSION_GRANTED &&
               resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForCameraAndMicrophone(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    private void createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(this, true);

        // Share your camera
        cameraCapturer = new CameraCapturer(this, CameraSource.FRONT_CAMERA);
        localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer);
        primaryVideoView.setMirror(true);
        localVideoTrack.addRenderer(primaryVideoView);
        localVideoView = primaryVideoView;
    }

    private void setAccessToken() {
        // OPTION 1- Generate an access token from the getting started portal
        // https://www.twilio.com/console/video/dev-tools/testing-tools
        this.accessToken = TWILIO_ACCESS_TOKEN;

        // OPTION 2- Retrieve an access token from your own web app
        // retrieveAccessTokenfromServer();
    }

    private void connectToRoom(String roomName) {
        configureAudio(true);
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                .roomName(roomName);

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (localAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(Collections.singletonList(localAudioTrack));
        }

        /*
         * Add local video track to connect options to share with participants.
         */
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
        }
        room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
        setDisconnectAction();
    }

    /*
     * The initial state when there is no active room.
     */
    private void intializeUI() {
        connectActionFab.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.ic_call_white_24px));
        connectActionFab.show();
        connectActionFab.setOnClickListener(connectActionClickListener());
        switchCameraActionFab.show();
        switchCameraActionFab.setOnClickListener(switchCameraClickListener());
        localVideoActionFab.show();
        localVideoActionFab.setOnClickListener(localVideoClickListener());
        muteActionFab.show();
        muteActionFab.setOnClickListener(muteClickListener());
    }

    /*
     * The actions performed during disconnect.
     */
    private void setDisconnectAction() {
        connectActionFab.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.ic_call_end_white_24px));
        connectActionFab.show();
        connectActionFab.setOnClickListener(disconnectClickListener());
    }

    /*
     * Creates an connect UI dialog
     */
    private void showConnectDialog() {
        EditText roomEditText = new EditText(this);
        alertDialog = Dialog.createConnectDialog(roomEditText,
                connectClickListener(roomEditText), cancelConnectDialogClickListener(), this);
        alertDialog.show();
    }

    /*
     * Called when participant joins the room
     */
    private void addParticipant(Participant participant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            Snackbar.make(connectActionFab,
                    "Multiple participants are not currently support in this UI",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        participantIdentity = participant.getIdentity();
        videoStatusTextView.setText("Participant "+ participantIdentity + " joined");

        /*
         * Add participant renderer
         */
        if (participant.getVideoTracks().size() > 0) {
            addParticipantVideo(participant.getVideoTracks().get(0));
        }

        /*
         * Start listening for participant events
         */
        participant.setListener(participantListener());
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private void addParticipantVideo(VideoTrack videoTrack) {
        moveLocalVideoToThumbnailView();
        primaryVideoView.setMirror(false);
        videoTrack.addRenderer(primaryVideoView);
    }

    private void moveLocalVideoToThumbnailView() {
        if (thumbnailVideoView.getVisibility() == View.GONE) {
            thumbnailVideoView.setVisibility(View.VISIBLE);
            localVideoTrack.removeRenderer(primaryVideoView);
            localVideoTrack.addRenderer(thumbnailVideoView);
            localVideoView = thumbnailVideoView;
            thumbnailVideoView.setMirror(cameraCapturer.getCameraSource() ==
                    CameraSource.FRONT_CAMERA);
        }
    }

    /*
     * Called when participant leaves the room
     */
    private void removeParticipant(Participant participant) {
        videoStatusTextView.setText("Participant "+participant.getIdentity()+ " left.");
        if (!participant.getIdentity().equals(participantIdentity)) {
            return;
        }

        /*
         * Remove participant renderer
         */
        if (participant.getVideoTracks().size() > 0) {
            removeParticipantVideo(participant.getVideoTracks().get(0));
        }
        participant.setListener(null);
        moveLocalVideoToPrimaryView();
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeRenderer(primaryVideoView);
    }

    private void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            localVideoTrack.removeRenderer(thumbnailVideoView);
            thumbnailVideoView.setVisibility(View.GONE);
            localVideoTrack.addRenderer(primaryVideoView);
            localVideoView = primaryVideoView;
            primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                    CameraSource.FRONT_CAMERA);
        }
    }

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                localParticipant = room.getLocalParticipant();
                Toast.makeText(getApplicationContext(),"CONNECTED TO ROOM",Toast.LENGTH_SHORT).show();
                videoStatusTextView.setText("Connected to " + room.getName());
                //setTitle(room.getName());

                for (Participant participant : room.getParticipants()) {
                    addParticipant(participant);
                    break;
                }
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                videoStatusTextView.setText("Failed to connect");
                configureAudio(false);
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                localParticipant = null;
                videoStatusTextView.setText("Disconnected from " + room.getName());
                Toast.makeText(getApplicationContext(),"DISCONNECTED FROM ROOM",Toast.LENGTH_SHORT).show();

                VideoActivity.this.room = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    configureAudio(false);
                    intializeUI();
                    moveLocalVideoToPrimaryView();
                }
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                addParticipant(participant);

            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                removeParticipant(participant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }

    private Participant.Listener participantListener() {
        return new Participant.Listener() {
            @Override
            public void onAudioTrackAdded(Participant participant, AudioTrack audioTrack) {
                videoStatusTextView.setText("onAudioTrackAdded");
            }

            @Override
            public void onAudioTrackRemoved(Participant participant, AudioTrack audioTrack) {
                videoStatusTextView.setText("onAudioTrackRemoved");
            }

            @Override
            public void onVideoTrackAdded(Participant participant, VideoTrack videoTrack) {
                videoStatusTextView.setText("onVideoTrackAdded");
                addParticipantVideo(videoTrack);
            }

            @Override
            public void onVideoTrackRemoved(Participant participant, VideoTrack videoTrack) {
                videoStatusTextView.setText("onVideoTrackRemoved");
                removeParticipantVideo(videoTrack);
            }

            @Override
            public void onAudioTrackEnabled(Participant participant, AudioTrack audioTrack) {

            }

            @Override
            public void onAudioTrackDisabled(Participant participant, AudioTrack audioTrack) {

            }

            @Override
            public void onVideoTrackEnabled(Participant participant, VideoTrack videoTrack) {

            }

            @Override
            public void onVideoTrackDisabled(Participant participant, VideoTrack videoTrack) {

            }
        };
    }

    private DialogInterface.OnClickListener connectClickListener(final EditText roomEditText) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Connect to room
                 */

                connectToRoom(roomEditText.getText().toString());
            }
        };
    }

    private View.OnClickListener disconnectClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Disconnect from room
                 */
                if (room != null) {
                    room.disconnect();
                }
                intializeUI();
            }
        };
    }

    private View.OnClickListener connectActionClickListener() {
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showConnectDialog();
            }
        };
    }

    private DialogInterface.OnClickListener cancelConnectDialogClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intializeUI();
                alertDialog.dismiss();
            }
        };
    }

    private View.OnClickListener switchCameraClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraCapturer != null) {
                    CameraSource cameraSource = cameraCapturer.getCameraSource();
                    cameraCapturer.switchCamera();
                    if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                        thumbnailVideoView.setMirror(cameraSource == CameraSource.BACK_CAMERA);
                    } else {
                        primaryVideoView.setMirror(cameraSource == CameraSource.BACK_CAMERA);
                    }
                }
            }
        };
    }

    private View.OnClickListener localVideoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Enable/disable the local video track
                 */
                if (localVideoTrack != null) {
                    boolean enable = !localVideoTrack.isEnabled();
                    localVideoTrack.enable(enable);
                    int icon;
                    if (enable) {
                        icon = R.drawable.ic_videocam_green_24px;
                        switchCameraActionFab.show();
                    } else {
                        icon = R.drawable.ic_videocam_off_red_24px;
                        switchCameraActionFab.hide();
                    }
                    localVideoActionFab.setImageDrawable(
                            ContextCompat.getDrawable(VideoActivity.this, icon));
                }
            }
        };
    }

    private View.OnClickListener muteClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Enable/disable the local audio track. The results of this operation are
                 * signaled to other Participants in the same Room. When an audio track is
                 * disabled, the audio is muted.
                 */
                if (localAudioTrack != null) {
                    boolean enable = !localAudioTrack.isEnabled();
                    localAudioTrack.enable(enable);
                    int icon = enable ?
                            R.drawable.ic_mic_green_24px : R.drawable.ic_mic_off_red_24px;
                    muteActionFab.setImageDrawable(ContextCompat.getDrawable(
                            VideoActivity.this, icon));
                }
            }
        };
    }

    private void retrieveAccessTokenfromServer() {
        Ion.with(this)
                .load("http://172.16.110.102:3000/tokens")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            VideoActivity.this.accessToken = result.get("token").getAsString();
                        } else {
                            Toast.makeText(VideoActivity.this,
                                    R.string.error_retrieving_access_token, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    private void configureAudio(boolean enable) {
        if (enable) {
            previousAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            /*
             * Always disable microphone mute during a WebRTC call.
             */
            previousMicrophoneMute = audioManager.isMicrophoneMute();
            audioManager.setMicrophoneMute(false);
        } else {
            audioManager.setMode(previousAudioMode);
            audioManager.abandonAudioFocus(null);
            audioManager.setMicrophoneMute(previousMicrophoneMute);
        }
    }

   /* public void screenShot(View view) {
        captureScreenShot = (Button) findViewById(R.id.but);
       imageView = (ImageView)findViewById(R.id.vid);
        mbitmap = getBitmapOFRootView(captureScreenShot);
        imageView.setImageBitmap(mbitmap);
        createImage(mbitmap);
    }

    public Bitmap getBitmapOFRootView(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        Bitmap bitmap1 = rootview.getDrawingCache();
        return bitmap1;
    }

    public void createImage(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File file = new File(Environment.getExternalStorageDirectory() +
                "/capturedscreenandroid.jpg");
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes.toByteArray());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
  /*public Bitmap screenShot(View view) {
       Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(bitmap);

       view.draw(canvas);
       return bitmap;
   }
   private void takeScreenshot() {
   //    TextureView mPreview=(TextureView)findViewById(video_container);
       //FrameLayout f =(FrameLayout) findViewById(R.id.video_container);

       // View v1=f;
      Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file*/
        //   String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
          //  View v1=f;
         /*   View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
         //    Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }
   private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

/*public void takeScreenshot()
{
    String mPath = Environment.getExternalStorageDirectory().toString() + "/" + ".jpg";
    View memecontentView = findViewById(R.id.frame_memecontent);
    View v1 = memecontentView;
    v1.setDrawingCacheEnabled(true);
    v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
    v1.buildDrawingCache(true);
   Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
   File f = new File(mPath + File.separator + value);
    try {
        f.createNewFile();
        // write the bytes in file
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
    }catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    Toast.makeText(getApplicationContext(),"Image Saved at " + f.getPath(), Toast.LENGTH_SHORT).show();
    v1.destroyDrawingCache();


}*/
    /*  public void onclicking(View v)
    {
       takeScreenshot();
    //    cameraCapturer.takePicture(photographer);
    }*/
    public void capturepic(View v)
    {
        Bitmap bitmap = takeScreenshot();
        saveBitmap(bitmap);
        shareIt();
    }


    public Bitmap takeScreenshot() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public void saveBitmap(Bitmap bitmap) {
       File imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
        FileOutputStream fos;
        try {

            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }
    private void shareIt() {
        File imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
        Uri uri = Uri.fromFile(imagePath);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        String shareBody = "In Tweecher, My highest score with screen shot";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Tweecher score");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

}
