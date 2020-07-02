package com.appbestsmile.voicelikeme.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.chat.UserItem;
import com.appbestsmile.voicelikeme.chat.MessageItem;
import com.appbestsmile.voicelikeme.chat.MessageListAdapter;
import com.appbestsmile.voicelikeme.chat.WaitProgressDialog;
import com.appbestsmile.voicelikeme.global.AppPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appbestsmile.voicelikeme.R.color.chat_main_dark;

public class ChatMessageActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private final int FILE_OPEN_REQUEST_CODE = 1000;

    public String topic_id;
    private Uri selectedFile;
    private Context mContext;

    ListView listView;
    MessageListAdapter messageListAdapter;
    ArrayList<MessageItem> listMessages;
    ArrayList<UserItem> listUsers;

    Toolbar toolbar;
    EditText editMessage;
    RelativeLayout layoutFullPhoto;
    ImageView imageFullPhoto;
    WaitProgressDialog dialogUpload;

    private static final int LENGTH_NICKNAME = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        mContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            topic_id = extras.getString("topic_id");
        }

        editMessage = findViewById(R.id.editMessage);
        findViewById(R.id.btnSender).setOnClickListener(this);
        findViewById(R.id.btnFile).setOnClickListener(this);

        layoutFullPhoto = (RelativeLayout) findViewById(R.id.layoutFullPhoto);
        imageFullPhoto = (ImageView) findViewById(R.id.imageFullPhoto);

        // =========================        Manage Action and status bar            ====================== //

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.md_black_1000));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(AppPreference.getInstance().GetNickname());
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);

        listView = (ListView) findViewById(R.id.listMessage);
        listMessages = new ArrayList<MessageItem>();


        // Access a Cloud Firestore instance from your Activity
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        WaitProgressDialog loadingDialog = new WaitProgressDialog(this, "Loading. Please wait...");
        loadingDialog.show();


        CollectionReference usersCollectionRef = db.collection("users");
        usersCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    listUsers = new ArrayList<UserItem>();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String user_id        = document.getId();
                        String nickname       = document.getString("nickname");
                        String profile_image  = document.getString("profile_image");

                        listUsers.add(new UserItem(user_id, nickname, profile_image));
                    }

                    CollectionReference messagesCollectionRef = db.collection("messages");
                    messagesCollectionRef.whereEqualTo("topic_id", topic_id).orderBy("timestamp").get()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("bruce", e.getMessage());
                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                String today = "";
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");


                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String message          = document.getString("message");
                                    String timestamp        = document.getString("timestamp");
                                    String mediaPath        = document.getString("media_path");
                                    String user_id          = document.getString("user_id");
                                    List<String> liked_users = (List<String>) document.get("liked_users");

                                    Log.d(TAG, "Timestamp : " + timestamp);

                                    for(int i = 0; i < listUsers.size(); i++){

                                        UserItem submitter = listUsers.get(i);

                                        if(submitter.id.compareTo(user_id) == 0){

                                            /*String dateString = formatter.format(new Date(Long.parseLong(timestamp)));
                                            if(today.compareTo(dateString) != 0)
                                            {
                                                today = dateString;
                                                listMessages.add(new MessageItem(timestamp));
                                            }*/

                                            String messageNickname = String.format("User_%s", user_id.substring(0, LENGTH_NICKNAME));
                                            listMessages.add(new MessageItem(document.getId(), submitter.id, submitter.nickname, submitter.profileImage, messageNickname, message, timestamp, mediaPath, liked_users));
                                            break;
                                        }
                                    }
                                }

                                if(listMessages.size() != 0)
                                {
                                    messageListAdapter = new MessageListAdapter(mContext, R.layout.chat_message_item, listMessages);
                                    listView.setAdapter(messageListAdapter);
                                }
                            }

                            loadingDialog.dismiss();
                        }
                    });
                }
            }
        });


        dialogUpload = new WaitProgressDialog(this, "Uploading. Please wait...");
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.btnSender :

                dialogUpload.show();

                if(selectedFile != null){

                    InputStream iStream = null;
                    byte[] bbytes = null;

                    try {
                        iStream = getContentResolver().openInputStream(selectedFile);
                        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];

                        int len = 0;
                        while ((len = iStream.read(buffer)) != -1) {
                            byteBuffer.write(buffer, 0, len);
                        }
                        bbytes = byteBuffer.toByteArray();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    String storageFileName = String.format("messages/%s", getNameFromURI(selectedFile));

                    StorageReference messagesStorageRef = storageRef.child(storageFileName);
                    UploadTask uploadTask = messagesStorageRef.putBytes(bbytes);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads

                            Log.d(TAG, "uploadtask exception : " + exception.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            createMessageOnFirebase(getNameFromURI(selectedFile));
                            selectedFile = null;
                        }
                    });

                }else{

                    String strMessage = editMessage.getText().toString();
                    createMessageOnFirebase(strMessage);
                }

                break;

            case R.id.btnFile :

                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_OPEN_REQUEST_CODE);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FILE_OPEN_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri selectedfile = data.getData(); //The uri with the location of the file
            this.selectedFile = selectedfile;

            Log.d(TAG, selectedfile.getPath());

            String filename = getNameFromURI(selectedfile);
            editMessage.setText(filename);
        }
    }

    public String getNameFromURI(Uri uri) {
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }


    protected void createMessageOnFirebase(String strMessage){

        String user_id = FirebaseAuth.getInstance().getUid();

        Map<String, Object> message = new HashMap<>();
        message.put("topic_id", topic_id);
        message.put("user_id", user_id);
        message.put("message", strMessage);
        message.put("timestamp", System.currentTimeMillis() + "");
        message.put("liked_users", new ArrayList<String>());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("messages")
                .add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        editMessage.setText("");
                        dialogUpload.dismiss();

                        restart();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void restart(){
        finish();
        startActivity(getIntent().putExtra("topic_id", topic_id));
    }

    public void showFullImage(String imagePath){

        toolbar.setVisibility(View.GONE);
        layoutFullPhoto.setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageFullPhoto.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {

        if(layoutFullPhoto.getVisibility() == View.VISIBLE){
            toolbar.setVisibility(View.VISIBLE);
            layoutFullPhoto.setVisibility(View.GONE);
        }
        else
            super.onBackPressed();
    }
}
