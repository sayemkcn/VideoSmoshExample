package xyz.rimon.videomash;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import xyz.rimon.videomash.utils.StorageUtil;
import xyz.rimon.videomash.utils.Toaster;

import static xyz.rimon.videomash.utils.MediaAssistant.initRecorder;
import static xyz.rimon.videomash.utils.MediaAssistant.prepareRecorder;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SurfaceHolder.Callback {

    private MediaRecorder recorder;
    private SurfaceHolder holder;
    boolean recording = false;

    private ImageButton btnRecord;
    private Button btnDelete;
    private Button btnNext;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        recorder = new MediaRecorder();
//        initRecorder();
        setContentView(R.layout.activity_main);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface_camera);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);

        this.btnRecord = this.findViewById(R.id.btnRecord);
        this.btnRecord.setOnTouchListener(this);

        this.btnDelete = this.findViewById(R.id.btnDelete);
        this.btnDelete.setOnTouchListener(this);
        this.btnNext = this.findViewById(R.id.btnNext);
        this.btnNext.setOnTouchListener(this);

        this.progressBar = findViewById(R.id.progress);

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        recorder = initRecorder(this.recorder);
        recorder = prepareRecorder(this, this.recorder, surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        finish();
    }

//    @Override
//    public void onClick(View view) {
//        if (recording) {
//            recorder.stop();
//            recording = false;
//
//            // Let's initRecorder so we can record again
//            this.recorder = initRecorder(this.recorder);
//            this.recorder = prepareRecorder(this, this.recorder, this.holder);
//        } else {
//
//            recording = true;
//            recorder.start();
//        }
//    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        if (id == R.id.btnRecord) {
            this.onRecordButtonPressed(motionEvent);
        } else if (id == R.id.btnDelete) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                StorageUtil.clearLatest(this);
                this.progressBar.setProgress(StorageUtil.getNumberOfFiles(this));
            }
        } else if (id == R.id.btnNext) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                this.onNextButtonClick();
        }
        return false;
    }

    private void onNextButtonClick() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Processing..");
        pd.show();
        String fileName = StorageUtil.mergeObjects(this, StorageUtil.MERGED_FILE_NAME);
        if (fileName != null)
            Toaster.toast(this, "Success!\nFile can be found here: " + fileName);
        else
            Toaster.toast(this, "Failed!");
        pd.dismiss();
        this.progressBar.setProgress(0);
    }

    private void onRecordButtonPressed(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            recording = true;
            recorder.start();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            recorder.stop();
            recording = false;
            StorageUtil.moveFile(this, System.currentTimeMillis() + ".mp4");
            this.progressBar.setProgress(StorageUtil.getNumberOfFiles(this));
            // Let's initRecorder so we can record again
            this.recorder = initRecorder(this.recorder);
            this.recorder = prepareRecorder(this, this.recorder, this.holder);
        } else {
            Log.i("MOTION_EVENT", motionEvent.toString());
        }
    }


}
