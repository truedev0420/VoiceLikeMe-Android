package com.appbestsmile.voicelikeme.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.chat.CircleImageView;

import static com.appbestsmile.voicelikeme.R.drawable.chat_profile_background;

public class ChatLoginActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton profileImage;
    CircleImageView imageProfile;

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

        imageProfile = (CircleImageView) findViewById(R.id.imageProfile);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.btn_profileImage :
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
                break;

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            /*case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageProfile.setImageURI(selectedImage);
                }

                break;*/

            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageProfile.setImageURI(selectedImage);
                }
                break;
        }
    }
}
