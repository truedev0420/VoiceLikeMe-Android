package com.appbestsmile.voicelikeme.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.activities.ChatMessageActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageListAdapter extends ArrayAdapter<MessageItem> {

    private final String TAG = getClass().getSimpleName();
    private int resourceLayout;
    private boolean isZoomClicked = false;
    private Context mContext;
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm a");
    SimpleDateFormat firstMessageFormatter = new SimpleDateFormat("yyyy/MM/dd");

    FirebaseUser currentUser;

    public MessageListAdapter(Context context, int resource, List<MessageItem> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        MessageItem messageItem = getItem(position);

        if (messageItem != null) {

            TextView textAvatarNickname = (TextView) v.findViewById(R.id.avatarNickname);
            TextView textNickname = (TextView) v.findViewById(R.id.textNickname);
            TextView textMessage = (TextView) v.findViewById(R.id.textMessage);
            TextView textTimeStamp  = (TextView) v.findViewById(R.id.textTimestamp);
            TextView textLikes      = (TextView) v.findViewById(R.id.textLikes);

            CircleImageView userAvatar = (CircleImageView) v.findViewById(R.id.avatarImage);


            if(messageItem.isFirstSent){

                RelativeLayout layoutDate = (RelativeLayout) v.findViewById(R.id.layoutDate);
                LinearLayout layoutMessage = (LinearLayout) v.findViewById(R.id.layoutMessage);
                TextView textDate = (TextView) v.findViewById(R.id.textDate);

                layoutDate.setVisibility(View.VISIBLE);
                layoutMessage.setVisibility(View.GONE);

                String dateString = firstMessageFormatter.format(new Date(Long.parseLong(messageItem.timestamp)));
                textDate.setText(dateString);

            }else {

                int likes = 0;
                if(messageItem.likedUsers != null)
                    likes = messageItem.likedUsers.size();

                textLikes.setText(String.format("%d likes", likes));


                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                // =====            Set user avatar             ===== //

                if(userAvatar != null && !messageItem.userAvatar.isEmpty()){

                    String filePath = String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(), messageItem.userAvatar.replace("/", "_"));

                    File localFile = new File(filePath);

                    try {
                        if(!localFile.exists()){
                            localFile.createNewFile();

                            storageRef.child(messageItem.userAvatar).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Local temp file has been created
                                    Bitmap bmp = BitmapFactory.decodeFile(localFile.getPath());
                                    userAvatar.setImageBitmap(bmp);
                                    textAvatarNickname.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                        }else{
                            Bitmap bmp = BitmapFactory.decodeFile(localFile.getPath());
                            userAvatar.setImageBitmap(bmp);
                            textAvatarNickname.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                // =====            Set other views             ===== //

                if (textAvatarNickname != null) {
                    textAvatarNickname.setText(messageItem.userNickname.substring(0, 2));
                }

                if(textNickname != null){
                    textNickname.setText(messageItem.userMessageNickname);
                }

                if (textMessage != null) {
                    textMessage.setText(messageItem.message);
                }

                if(textTimeStamp != null){
                    String dateString = formatter.format(new Date(Long.parseLong(messageItem.timestamp)));
                    textTimeStamp.setText(dateString);
                }

                // =====            Set user actions            ===== //

                ImageButton btnDownload = (ImageButton) v.findViewById(R.id.btnDownload);
                ImageButton btnZoom = (ImageButton) v.findViewById(R.id.btnZoom);
                ImageButton btnLike = (ImageButton) v.findViewById(R.id.btnLike);
                ImageButton btnPLay = (ImageButton) v.findViewById(R.id.btnPlay);


                String ext = messageItem.message.substring(messageItem.message.indexOf(".") + 1);
                String type = "message";

                if (ext.equalsIgnoreCase("jpg")
                        || ext.equalsIgnoreCase("png")
                        || ext.equalsIgnoreCase("jpeg")) {
                    type = "photo";

                } else if(ext.equalsIgnoreCase("wav")){
                    type = "audio";
                }

                if(btnDownload != null && btnZoom != null && btnLike != null && btnPLay != null){

                    switch (type){
                        case "message" :
                            btnDownload.setVisibility(View.GONE);
                            btnPLay.setVisibility(View.GONE);
                            btnZoom.setVisibility(View.GONE);
                            btnLike.setVisibility(View.VISIBLE);

                            textLikes.setVisibility(View.VISIBLE);

                            if(messageItem.likedUsers.contains(currentUser.getUid())){
                                btnLike.setImageResource(R.drawable.ic_favorite_fill_24dp);
                            }

                            break;

                        case "photo" :
                            btnDownload.setVisibility(View.VISIBLE);
                            btnPLay.setVisibility(View.GONE);
                            btnZoom.setVisibility(View.VISIBLE);
                            btnLike.setVisibility(View.VISIBLE);

                            textLikes.setVisibility(View.VISIBLE);

                            if(messageItem.likedUsers.contains(currentUser.getUid())){
                                btnLike.setImageResource(R.drawable.ic_favorite_fill_24dp);
                            }

                            break;

                        case "audio" :
                            btnDownload.setVisibility(View.VISIBLE);
                            btnPLay.setVisibility(View.VISIBLE);
                            btnZoom.setVisibility(View.GONE);
                            btnLike.setVisibility(View.GONE);

                            textLikes.setVisibility(View.GONE);

                            break;
                    }

                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            File localFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + messageItem.message);

                            WaitProgressDialog dialogDownload = new WaitProgressDialog(mContext, "Downloading. Please wait...");
                            dialogDownload.show();

                            try {
                                if(!localFile.exists()){
                                    localFile.createNewFile();



                                    storageRef.child("messages/" + messageItem.message).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Local temp file has been created

                                            Toast.makeText(mContext, "Downloaded successfully.",  Toast.LENGTH_LONG).show();
                                            dialogDownload.dismiss();

                                            if(isZoomClicked){
                                                ((ChatMessageActivity) mContext).showFullImage(localFile.getPath());
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                            dialogDownload.dismiss();
                                            Log.d(TAG, "Exception while downloading : " + exception.toString());
                                        }
                                    });
                                }else{
                                    Toast.makeText(mContext, "Downloaded successfully.",  Toast.LENGTH_LONG).show();
                                    dialogDownload.dismiss();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    // =====                Manage Like button          ===== //

                    FirebaseApp.initializeApp(mContext);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    btnLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(messageItem.likedUsers.contains(currentUser.getUid())){
                                Toast.makeText(mContext, "You have already liked this message.", Toast.LENGTH_SHORT);
                                return;
                            }

                            if(messageItem.userId.compareTo(currentUser.getUid()) == 0){
                                Toast.makeText(mContext, "You can't like your message.", Toast.LENGTH_SHORT);
                                return;
                            }

                            Map<String, Object> messageUpdated = new HashMap<>();
                            messageItem.likedUsers.add(currentUser.getUid());
                            messageUpdated.put("liked_users", messageItem.likedUsers);

                            db.collection("messages")
                                    .document(messageItem.documentId)
                                    .update(messageUpdated);

                            ((ChatMessageActivity) mContext).restart();
                        }
                    });


                    // =====                Manage Full screen view button          ===== //

                    btnZoom.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            isZoomClicked = true;
                            btnDownload.callOnClick();
                        }
                    });
                }
            }
        }
        return v;
    }

    public static void copy(File dst, File src) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
