package xyz.rimon.videomash.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public static Object readObjects(Context context, String fileName) {
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
        Object object = readObjects(context, MANIFEST_FILE_NAME);
        Stack<String> stack;
        if (object != null) stack = (Stack<String>) object;
        else stack = new Stack<>();
        stack.push(to.getAbsolutePath());
        writeObject(context, MANIFEST_FILE_NAME, stack);
    }

    public static int getNumberOfFiles(Context context) {
        Object object = readObjects(context, MANIFEST_FILE_NAME);
        Stack<String> stack;
        if (object == null) return 0;
        stack = (Stack<String>) object;
        return stack.size();
    }
}
