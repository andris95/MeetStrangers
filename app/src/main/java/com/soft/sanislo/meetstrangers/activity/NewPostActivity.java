package com.soft.sanislo.meetstrangers.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 24.09.16.
 */
public class NewPostActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.edt_post_text)
    EditText edtPostText;

    private static final String TAG = NewPostActivity.class.getSimpleName();

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference postReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        addSendPostEnabledListener();
    }

    private void addSendPostEnabledListener() {
        edtPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int count, int after) {
                if (!edtPostText.getText().toString().equals("")) {
                    mMenu.findItem(R.id.menu_send_post).setEnabled(true);
                } else {
                    mMenu.findItem(R.id.menu_send_post).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        menu.findItem(R.id.menu_send_post).setEnabled(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_post:
                sendPost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendPost() {
        String postText = edtPostText.getText().toString();
        if (postText.equals("")) return;

        String postKey = database.child(Constants.F_POSTS).child(uid).push().getKey();
        Post post = new Post(postText, uid, postKey, new Date().getTime());
        database.child(Constants.F_POSTS).child(uid).child(postKey).setValue(post)
                .addOnCompleteListener(postCompleteListener).addOnFailureListener(postFailureListener);
    }

    private OnCompleteListener<Void> postCompleteListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            Toast.makeText(getApplicationContext(), "Posted successfully", Toast.LENGTH_SHORT).show();
        }
    };
    private OnFailureListener postFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Posting failed, please try again later...", Toast.LENGTH_SHORT).show();
        }
    };
}
