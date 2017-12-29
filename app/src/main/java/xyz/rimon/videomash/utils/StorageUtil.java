package xyz.rimon.videomash.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

/**
 * Created by SAyEM on 29/12/17.
 */

public class StorageUtil {
    private StorageUtil() {
    }

    private static String uniqueID = UUID.randomUUID().toString();
    public static String MANIFEST_FILE_NAME = "manifest.lol";
    public static String TEMP_FILE_NAME = "/sdcard/temp.mp4";
    public static String MERGED_FILE_NAME = "video.mp4";

    public static void writeObject(Context context, String fileName, Object object) {
        // check permission
        if (!PermissionUtil.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            PermissionUtil.askForPermission((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        File rootPath = Environment.getExternalStorageDirectory();
        File objectDir = new File(rootPath.getAbsolutePath() + "/" + context.getPackageName());
        if (!objectDir.exists()) objectDir.mkdirs();
        File objectsFile = new File(objectDir, fileName);
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(objectsFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static Object readObject(Context context, String fileName) {
        // check permission
        if (!PermissionUtil.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE))
            PermissionUtil.askForPermission((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE);

        Object object = null;
        File rootPath = Environment.getExternalStorageDirectory();
        File cmedDir = new File(rootPath.getAbsolutePath() + "/" + context.getPackageName());
        if (!cmedDir.exists())
            cmedDir.mkdirs();
        File objectsFile = new File(cmedDir, fileName);
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(objectsFile);
            ois = new ObjectInputStream(fis);
            object = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("CAN\'T READ OBJECTS ", e.toString());
        } finally {
            try {
                if (ois != null) ois.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return object;
    }

//    public static void writeObject(Context context, String fileName, Object object) {
//        List<Object> objectList = StorageUtil.readObjects(context, fileName);
//        if (objectList == null)
//            objectList = new ArrayList<>();
//        objectList.add(object);
//        StorageUtil.writeObjects(context, fileName, objectList);
//        Log.i("OFFLINE_objectS", String.valueOf(objectList.size()));
//    }

//    public static void clearObjects(Context context, String fileName) {
//        writeObjects(context, fileName, new ArrayList<Object>());
//    }

    public static void moveFile(Context context, String dest) {
        // check permission
        if (!PermissionUtil.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE))
            PermissionUtil.askForPermission((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE);

        File from = new File(StorageUtil.TEMP_FILE_NAME);
        File toDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName());
        if (!toDir.exists()) toDir.mkdirs();
        File to = new File(toDir, dest);
        from.renameTo(to);

        // add file to manifest
        Object object = readObject(context, MANIFEST_FILE_NAME);
        Stack<String> stack;
        if (object != null) stack = (Stack<String>) object;
        else stack = new Stack<>();
        stack.push(to.getAbsolutePath());
        writeObject(context, MANIFEST_FILE_NAME, stack);

        Logger.i(stack.toString());
    }

    public static int getNumberOfFiles(Context context) {
        Object object = readObject(context, MANIFEST_FILE_NAME);
        Stack<String> stack;
        if (object == null) return 0;
        stack = (Stack<String>) object;
        return stack.size();
    }

    public static void clearLatest(Context context) {
        Object object = readObject(context, MANIFEST_FILE_NAME);
        Stack<String> stack;
        if (object == null) return;
        stack = (Stack<String>) object;
        if (stack.isEmpty()) return;
        String lastFileName = stack.get(stack.size() - 1);
        if (new File(lastFileName).delete())
            stack.pop();
        writeObject(context, MANIFEST_FILE_NAME, stack);
    }

    public static String mergeObjects(Context context, String fileName) {
        Object object = readObject(context, MANIFEST_FILE_NAME);
        Stack<String> stack;
        if (object == null) stack = new Stack<>();
        stack = (Stack<String>) object;

        // prepare output file
        File outputDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName());
        if (!outputDir.exists()) outputDir.mkdirs();
        File outputFile = new File(outputDir, fileName);

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            assert stack != null;
            for (int i = 0; i < stack.size(); i++) {
                File f = new File(stack.get(i));
                fis = new FileInputStream(f);
                boolean append = i != 0;
                fos = new FileOutputStream(outputFile, append);
                IOUtils.copy(fis, fos);

                f.delete();
            }
            writeObject(context, MANIFEST_FILE_NAME, new Stack<>());
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("CAN\'T READ OBJECTS ", e.toString());
        } finally {
            try {
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static void mergeVideos(Context context, List<InputStream> inputStreams, File outputFile) throws IOException {
        MovieCreator mc = new MovieCreator();
        Movie movie = new Movie();
        for (InputStream i : inputStreams) {
            Movie video = mc.build((DataSource) Channels.newChannel(i));
            for (Track t : video.getTracks()) {
                movie.addTrack(t);
            }

        }

//        Movie video = mc.build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-video.mp4")));
//        Movie audio = mc.build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-english-audio.mp4")));

//        List<Track> videoTracks = movie.getTracks();
//        video.setTracks(new LinkedList());
//
//        List<Track> audioTracks = audio.getTracks();
//
//        for (Track videoTrack : videoTracks) {
//            video.addTrack(new AppendTrack(videoTrack, videoTrack));
//        }
//
//        for (Track audioTrack : audioTracks) {
//            video.addTrack(new AppendTrack(audioTrack, audioTrack));
//        }

        IsoFile out = (IsoFile) new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(outputFile);
        out.getBox(fos.getChannel());
        fos.close();
    }
}
