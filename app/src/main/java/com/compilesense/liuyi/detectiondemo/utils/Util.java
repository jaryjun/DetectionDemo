package com.compilesense.liuyi.detectiondemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.compilesense.liuyi.detectiondemo.R;
import com.compilesense.liuyi.detectiondemo.activity.PersonManageActivity;
import com.compilesense.liuyi.detectiondemo.model.Group;
import com.compilesense.liuyi.detectiondemo.model.Person;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by shenjingyuan002 on 16/9/5.
 */
public class Util {

    public interface DialogOnClickListener{
        void onClick(int which);
        void onPosiButtonClick(int which,String text1,String text2);
    }
    public static void buildImgGetDialog(Context context, final DialogOnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String[] a = {"相册","拍照"};
        builder.setTitle(context.getString(R.string.dialog_img_get_title))
                .setItems(a, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onClick(which);
                    }
                })
                .create().show();
    }

    public static void buildChosePersonDialog(Context context, List<Person> personList, final DialogOnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (personList == null || personList.isEmpty()){
            builder.setTitle(context.getString(R.string.dialog_person_chose))
                    .create().show();
            return;
        }
        String[] a = new String[personList.size()];

        for (int i = 0 ; i < personList.size() ; i++){
            a[i] = personList.get(i).person_name;
        }

        builder.setTitle(context.getString(R.string.dialog_person_chose))
                .setItems(a, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onClick(which);
                    }
                })
                .create().show();
    }

    public static void buildChooseGroupDialog(Context context, List<Group> groupList, final DialogOnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (groupList == null || groupList.isEmpty()){
            builder.setTitle(context.getString(R.string.dialog_person_chose))
                    .create().show();
            return;
        }
        String[] a = new String[groupList.size()];

        for (int i = 0 ; i < groupList.size() ; i++){
            a[i] = groupList.get(i).group_name;
        }

        builder.setTitle(context.getString(R.string.dialog_person_chose))
                .setItems(a, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onClick(which);
                    }
                })
                .create().show();
    }
    //更改账号对话框
    public static void buildEditDialog(final Context context,String title,String str1,String str2,final DialogOnClickListener listener){

        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_chage_acount,null);
        final EditText text1 = (EditText) dialogView.findViewById(R.id.edit_text1);
        final EditText text2 = (EditText) dialogView.findViewById(R.id.edit_text2);
        TextView textView1 = (TextView) dialogView.findViewById(R.id.text_view1);
        TextView textView2 = (TextView) dialogView.findViewById(R.id.text_view2);
        textView1.setText(str1);
        textView2.setText(str2);
        customizeDialog.setTitle("更改账号");
        customizeDialog.setTitle(title);
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String info1=text1.getText().toString();
                        String info2=text2.getText().toString();
                        if (TextUtils.isEmpty(info1) || TextUtils.isEmpty(info2)){
                            Toast.makeText(context, "更改失败，不能有空数据", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        listener.onPosiButtonClick(which,info1,info2);
                    }
                });
        customizeDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
        customizeDialog.show();

    }





    public static String string2jsonString(String s){

        s = s.replace("\\","");
        int length = s.length();
        s = s.substring(1,length -1);

        int a = s.indexOf("[");
        int b = s.indexOf("]");

        if (a != -1 && b != -1){
            String r;
            if (s.charAt(a - 1) == '\"'){
                String aa = s.substring(0, a - 1 );
                String bb = s.substring(a, b + 1);
                String cc = s.substring(b + 2);
                r = aa + bb + cc;
                return r;
            }
        }
        return s;
    }

    public static Bitmap getBitmapFromAssets(Context context){
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("jiang.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] bitmap2ByteArray(Bitmap bitmap){
        ByteArrayOutputStream bStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bStream);
        return bStream.toByteArray();
    }

    public static byte[] uri2ByteArray(Uri uri,Context context)throws IOException {
        // this dynamically extends to take the bytes you read
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static void compressPicture(String srcPath, String desPath) {
        FileOutputStream fos = null;
        BitmapFactory.Options op = new BitmapFactory.Options();

        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        op.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, op);
        op.inJustDecodeBounds = false;

        // 缩放图片的尺寸
        float w = op.outWidth;
        float h = op.outHeight;
        float hh = 1024f;//
        float ww = 1024f;//
        // 最长宽度或高度1024
        float be = 1.0f;
        if (w > h && w > ww) {
            be = (float) (w / ww);
        } else if (w < h && h > hh) {
            be = (float) (h / hh);
        }
        if (be <= 0) {
            be = 1.0f;
        }
        op.inSampleSize = (int) be;// 设置缩放比例,这个数字越大,图片大小越小.
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, op);
        int desWidth = (int) (w / be);
        int desHeight = (int) (h / be);
        bitmap = Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
        try {
            fos = new FileOutputStream(desPath);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
