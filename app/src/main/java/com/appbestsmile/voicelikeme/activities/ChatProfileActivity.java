package com.appbestsmile.voicelikeme.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.chat.CircleImageView;
import com.appbestsmile.voicelikeme.chat.WaitProgressDialog;
import com.appbestsmile.voicelikeme.global.AppPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ChatProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = ChatProfileActivity.class.getSimpleName();

    ImageButton profileImage;
    CircleImageView imageProfile;
    Button btnUpdate;
    EditText editNickname;
    Uri selectedProfileImage;
    String auth_id;
    String old_nickname;
    String mOldProfileImage;
    boolean changedProfileImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_profile);

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
        if(!nickName.isEmpty()){
            old_nickname = nickName;
            editNickname.setText(nickName);
        }


        String profileImagePath = AppPreference.getInstance().GetProfileImage();
        if(!profileImagePath.isEmpty())
        {
            Bitmap bmp = BitmapFactory.decodeFile(profileImagePath);
            imageProfile.setImageBitmap(bmp);
        }


        auth_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(auth_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mOldProfileImage = document.getString("profile_image");
                    } else {
                        mOldProfileImage = "";
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
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

                // =====            Save user profile to local              ===== //

                AppPreference.getInstance().SetNickname(nickname);


                FirebaseApp.initializeApp(this);
                FirebaseFirestore db = FirebaseFirestore.getInstance();


                WaitProgressDialog dialogUpdating = new WaitProgressDialog(this, "Updating. Please wait...");
                dialogUpdating.show();


                if(changedProfileImage){

                    String destFilePath = Savefile(selectedProfileImage);
                    AppPreference.getInstance().SetProfileImage(destFilePath);


                    Bitmap bitmap = BitmapFactory.decodeFile(destFilePath);
                    imageProfile.setImageBitmap(bitmap);


                    // =====            Save user profile to firebase           ===== //


                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();


                    String extension = destFilePath.substring(destFilePath.lastIndexOf(".") + 1);
                    String storageFileName = String.format("users/%s.%s", nickname, extension);


                    StorageReference profileImagesRef = storageRef.child(storageFileName);


                    // =====            Upload Image file to cloud storage      ===== //

                    // Get the data from an ImageView as bytes
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    try {

                        UploadTask uploadTask = profileImagesRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads

                                Log.d(TAG, "uploadtask exception : " + exception.toString());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                String storageProfilePath = taskSnapshot.getMetadata().getPath();

                                Log.d(TAG, "storageProfilePath : " + storageProfilePath);

                                Map<String, Object> user = new HashMap<>();
                                user.put("nickname", nickname);
                                user.put("profile_image", storageProfilePath);

                                db.collection("users").document(auth_id)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialogUpdating.dismiss();
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                onBackPressed();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                dialogUpdating.dismiss();
                                                Log.w(TAG, "Error writing document", e);
                                                onBackPressed();
                                            }
                                        });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot snapshot) {
                                Log.d(TAG, "Uploaded bytes : " + snapshot.getBytesTransferred());
                            }
                        });
                    }catch(Exception e){
                        Log.d(TAG, "Exception while uploading : " + e.toString());
                    }

                }else{

                    Map<String, Object> user = new HashMap<>();
                    user.put("nickname", nickname);
                    user.put("profile_image", mOldProfileImage);

                    db.collection("users").document(auth_id)
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    dialogUpdating.dismiss();
                                    onBackPressed();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogUpdating.dismiss();
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                }

                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String filePath = GetUriPath(selectedImage);

                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    imageProfile.setImageBitmap(bitmap);
                    selectedProfileImage = selectedImage;
                    changedProfileImage = true;
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
