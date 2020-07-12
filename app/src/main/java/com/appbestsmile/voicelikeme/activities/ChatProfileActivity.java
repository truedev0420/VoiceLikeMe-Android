package com.appbestsmile.voicelikeme.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;


public class ChatProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = ChatProfileActivity.class.getSimpleName();

    ImageButton profileImage;
    Bitmap selectedBitmap;

    CircleImageView imageProfile;
    Button btnUpdate;
    EditText editNickname;
    Uri selectedProfileImage;
    String auth_id;
    String old_nickname;
    String mOldProfileImage;
    boolean changedProfileImage = false;
    final int CAMERA_PIC_REQUEST = 1000;

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        profileImage = (ImageButton) findViewById(R.id.btn_profileImage);
        profileImage.setOnClickListener(this);

        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);

        editNickname = findViewById(R.id.editNickname);
        imageProfile = (CircleImageView) findViewById(R.id.imageProfile);


        AppPreference.getInstance().Initialize(this);

        InitViews();

        checkCameraPermission();
    }

    private void InitViews() {

        String nickName = AppPreference.getInstance().GetNickname();
        if (!nickName.isEmpty()) {
            old_nickname = nickName;
            editNickname.setText(nickName);
        }


        String profileImagePath = AppPreference.getInstance().GetProfileImage();
        if (!profileImagePath.isEmpty()) {
            try {
                Bitmap bmp = BitmapFactory.decodeFile(profileImagePath);
                imageProfile.setImageBitmap(bmp);
            } catch (Exception e) {
                Log.e(TAG, "kfdjsla : " + e.toString());
            }
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

        switch (view.getId()) {

            case R.id.btn_profileImage:

//                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);

                break;

            case R.id.btnUpdate:

                String nickname = editNickname.getText().toString();

                if (nickname.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.chat_enter_nickname))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.dialog_action_ok), null)
                            .create()
                            .show();

                    break;
                }

                // =====            Save user profile to local              ===== //

                AppPreference.getInstance().SetNickname(nickname);


                FirebaseApp.initializeApp(this);
                FirebaseFirestore db = FirebaseFirestore.getInstance();


                WaitProgressDialog dialogUpdating = new WaitProgressDialog(this, "Updating. Please wait...");
                dialogUpdating.show();


                if (changedProfileImage) {

                    String destFilePath = SaveBitmap(selectedBitmap, nickname);
                    AppPreference.getInstance().SetProfileImage(destFilePath);


                    // =====            Save user profile to firebase           ===== //

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();


                    String extension = destFilePath.substring(destFilePath.lastIndexOf(".") + 1);
                    String storageFileName = String.format("users/%s.%s", nickname, extension);


                    StorageReference profileImagesRef = storageRef.child(storageFileName);


                    // =====            Upload Image file to cloud storage      ===== //

                    // Get the data from an ImageView as bytes
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                    } catch (Exception e) {
                        Log.d(TAG, "Exception while uploading : " + e.toString());
                    }

                } else {

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
        switch (requestCode) {
            case CAMERA_PIC_REQUEST:
                if (resultCode == RESULT_OK) {
                    try {
                        /*Uri selectedImage = imageReturnedIntent.getData();
                        String filePath = GetUriPath(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        selectedProfileImage = selectedImage;*/

                        Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                        imageProfile.setImageBitmap(bitmap);
                        selectedBitmap = bitmap;

                        changedProfileImage = true;
                    } catch (Exception e) {
                        Log.e("kfdjsaklfdj ", e.toString());
                    }
                }
                break;
        }
    }

    /*String Savefile(Uri sourceuri)
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
    }*/

    String SaveBitmap(Bitmap bitmap, String nickname) {

        String destinationFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + nickname + ".jpg";

        try {
            File avatar = new File(destinationFilename);

            if (avatar.exists()) {
                avatar.delete();
            }

            avatar.createNewFile();
            FileOutputStream out = new FileOutputStream(avatar);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            out.flush();
            out.close();

            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            Log.e(TAG, "kfjsdlak : " + e.toString());
        }

        return destinationFilename;
    }

    String GetUriPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00AF;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Permission not available requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
        } else {
            Log.d(TAG, "Permission has already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_USE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission was granted! Do your stuff");
                } else {
                    Log.d(TAG, "permission denied! Disable the function related with permission.");
                }
                return;
            }
        }

    }
}
