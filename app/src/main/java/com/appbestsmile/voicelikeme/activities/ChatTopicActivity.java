package com.appbestsmile.voicelikeme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.chat.TopicItem;
import com.appbestsmile.voicelikeme.chat.TopicListAdapter;
import com.appbestsmile.voicelikeme.global.AppPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatTopicActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final int RC_SIGN_IN = 10000;
    private FirebaseAuth mAuth;

    ListView listView;
    TopicListAdapter topicListAdapter;
    ArrayList<TopicItem> listTopics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_topic);


        mAuth = FirebaseAuth.getInstance();


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


        listView = (ListView) findViewById(R.id.listTopics);
        listTopics = new ArrayList<TopicItem>();


        // Access a Cloud Firestore instance from your Activity
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference topicsCollectionRef = db.collection("topics");


        ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage("Loading. Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        topicsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String title        = document.getString("title");
                        String createdDate  = document.getString("createdDate");
                        listTopics.add(new TopicItem(title, createdDate));
                    }

                    if(listTopics.size() != 0)
                    {
                        topicListAdapter = new TopicListAdapter(getApplicationContext(), R.layout.chat_topic_item, listTopics);
                        listView.setAdapter(topicListAdapter);
                    }
                }

                dialog.dismiss();
            }
        });

        /*String nickname = AppPreference.getInstance().GetNickname();
        if(nickname.isEmpty()){
            Intent intent = new Intent(this, ChatLoginActivity.class);
            startActivity(intent);
        }*/
    }

    @Override
    public void onStart() {

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.d(TAG, " signInAnonymously success : " + user.getUid());

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                            }
                        }
                    });

        }else{
            Log.d(TAG, "Current User : " + currentUser.getUid());
        }
    }

    // create an action bar buttonStorageException

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.chat_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btnProfile) {
            startActivity(new Intent(this, ChatLoginActivity.class).putExtra("current_user_id", mAuth.getCurrentUser().getUid()));
        }
        return super.onOptionsItemSelected(item);
    }


}
