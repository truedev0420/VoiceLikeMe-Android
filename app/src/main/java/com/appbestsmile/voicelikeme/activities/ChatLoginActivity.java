package com.appbestsmile.voicelikeme.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.chat.CircleImageView;
import com.appbestsmile.voicelikeme.global.AppPreference;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;


public class ChatLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = ChatLoginActivity.class.getSimpleName();

    ImageButton profileImage;
    CircleImageView imageProfile;
    Button btnUpdate;
    EditText editNickname;
    Uri selectedProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_login);

        // =========================        Manage Action and status bar            ====================== //

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.md_black_1000));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.tab_title_saved_recordings);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);


        profileImage = (ImageButton) findViewById(R.id.btn_profileImage);
        profileImage.setOnClickListener(this);

        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);

        editNickname = findViewById(R.id.editNickname);
        imageProfile = (CircleImageView) findViewById(R.id.imageProfile);

        InitViews();
    }

    private void InitViews(){

        String nickName = AppPreference.getInstance().GetNickname();
        if(!nickName.isEmpty())
            editNickname.setText(nickName);

        String profileImagePath = AppPreference.getInstance().GetProfileImage();
        if(!profileImagePath.isEmpty())
        {
            Bitmap bmp = BitmapFactory.decodeFile(profileImagePath);
            imageProfile.setImageBitmap(bmp);
        }
    }


    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.btn_profileImage :
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
                break;

            case R.id.btnUpdate :
                String nickname = editNickname.getText().toString();
               /* if(nickname == ""){
                    Toast.makeText(this, "Please enter your nickname or upload your image.", Toast.LENGTH_LONG);
                }else {*/

                    AppPreference.getInstance().SetNickname(nickname);

                    if(selectedProfileImage != null){
                        String destFilePath = Savefile(selectedProfileImage);
                        AppPreference.getInstance().SetProfileImage(destFilePath);

                        Log.d(TAG, destFilePath);

                        Bitmap bmp = BitmapFactory.decodeFile(destFilePath);
                        imageProfile.setImageBitmap(bmp);
                    }

                    onBackPressed();

//                }

                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageProfile.setImageURI(selectedImage);
                    selectedProfileImage = selectedImage;
                }
                break;
        }
    }

    String Savefile(Uri sourceuri)
    {
        String sourceFilename = sourceuri.getPath();
        String filePath = GetUriPath(sourceuri);

        String[] fileNames = filePath.split(File.separator);
        String fileName = fileNames[fileNames.length - 1];

        String destinationFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + fileName;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(filePath));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);

            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return destinationFilename;
    }

    String GetUriPath(Uri uri){

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
