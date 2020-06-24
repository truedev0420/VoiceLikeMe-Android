package com.appbestsmile.voicelikeme.activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChatTopicActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    ListView listView;
    TopicListAdapter topicListAdapter;
    ArrayList<TopicItem> listTopics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_topic);


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

        topicsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String title        = document.getString("title");
                        String createdDate  = document.getString("createdDate");

                        Log.d(TAG, title + " " + createdDate);

                        listTopics.add(new TopicItem(title, createdDate));
                    }

                    if(listTopics.size() == 0)
                        return;

                    topicListAdapter = new TopicListAdapter(getApplicationContext(), R.layout.chat_topic_item, listTopics);
                    listView.setAdapter(topicListAdapter);
                }
            }
        });


/*
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, token);
*/

        String nickname = AppPreference.getInstance().GetNickname();
        if(nickname.isEmpty()){
            Intent intent = new Intent(this, ChatLoginActivity.class);
            startActivity(intent);
        }
    }
}
