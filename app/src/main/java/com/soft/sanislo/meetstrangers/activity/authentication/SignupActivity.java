package com.soft.sanislo.meetstrangers.activity.authentication;

/**
 * Created by root on 04.09.16.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.BaseActivity;
import com.soft.sanislo.meetstrangers.activity.MainActivity;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends BaseActivity {

    @BindView(R.id.edt_email)
    EditText edtEmail;
    @BindView(R.id.edt_password)
    EditText edtPassword;

    @BindView(R.id.edt_first_name)
    EditText edtFirstName;
    @BindView(R.id.edt_last_name)
    EditText edtLastName;
    @BindView(R.id.sign_in_button)
    Button btnSignIn;
    @BindView(R.id.sign_up_button)
    Button btnSignUp;
    @BindView(R.id.btn_reset_password)
    Button btnResetPassword;
    @BindView(R.id.progressBar)
    ProgressBar pbProgressBar;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String email, password;
    private String mFirstName;
    private String mLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        database = Utils.getDatabase().getInstance();
        auth = FirebaseAuth.getInstance();
        reference = database.getReference();
    }

    @OnClick(R.id.sign_in_button)
    public void onClickSignIn() {
        finish();
    }

    @OnClick(R.id.sign_up_button)
    public void onClickSignUp() {
        mFirstName = edtFirstName.getText().toString().trim();
        mLastName = edtLastName.getText().toString().trim();
        boolean isValidName = Utils.isValidName(getApplicationContext(), mFirstName, mLastName);

        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        boolean isValidEmailPwrd = Utils.validateEmailPwrd(getApplicationContext(), email, password);

        if (isValidName && isValidEmailPwrd) {
            pbProgressBar.setVisibility(View.VISIBLE);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(onCreateUserCompleteListener);
        }
    }

    @OnClick(R.id.btn_reset_password)
    public void onClickResetPassword() {
        startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
    }

    private OnCompleteListener onCreateUserCompleteListener = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            pbProgressBar.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                makeToast("Authentication failed...");
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(onSignInCompleteListener);
            }
        }
    };

    private OnCompleteListener onSignInCompleteListener = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            FirebaseUser firebaseUser = auth.getCurrentUser();
            if (firebaseUser != null) {
                User user = new User();
                user.setUid(firebaseUser.getUid());
                user.setFirstName(mFirstName);
                user.setLastName(mLastName);
                user.setFullName(mFirstName + " " + mLastName);
                reference.child(Constants.F_USERS).child(firebaseUser.getUid()).setValue(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                finish();
                            }
                        });
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        pbProgressBar.setVisibility(View.GONE);
    }

}