package xyz.rimon.videomash;

import android.app.Activity;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by SAyEM on 29/12/17.
 */

public class MediaAssistant {

    static MediaRecorder initRecorder(MediaRecorder recorder) {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_480P);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(Constants.TEMP_FILE_NAME);
        recorder.setMaxDuration(500000); // 50 seconds
        recorder.setMaxFileSize(500000000); // Approximately 5 megabytes
        return recorder;
    }

    static MediaRecorder prepareRecorder(Activity context, MediaRecorder recorder, SurfaceHolder holder) {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
            return recorder;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            context.finish();
        } catch (IOException e) {
            e.printStackTrace();
            context.finish();
        }
        context = null;
        return null;
    }
}
