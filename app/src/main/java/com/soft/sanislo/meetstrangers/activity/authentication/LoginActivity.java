package com.soft.sanislo.meetstrangers.activity.authentication;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.BaseActivity;
import com.soft.sanislo.meetstrangers.activity.MainActivity;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.service.LocationService;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.btn_login_google)
    SignInButton btnLoginGoogle;

    @BindView(R.id.edt_email)
    EditText edtEmail;

    @BindView(R.id.password)
    EditText edtPassword;

    @BindView(R.id.btn_signup)
    Button btnSignUp;

    @BindView(R.id.progressBar)
    ProgressBar pbProgressBar;

    @BindView(R.id.btn_login)
    Button btnLogin;

    @BindView(R.id.btn_reset_password)
    Button btnResetPassword;

    public static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_GOOGLE_SIGNIN = 22228;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private String email, password;
    private User mUser;

    private OnCompleteListener<AuthResult> onSignInCompleteListener = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    private OnFailureListener onSignInFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            pbProgressBar.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mFirebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initGoogleSignIn();
    }

    private void initGoogleSignIn() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("568185669722-4o18jpcsav47fa23e8tten89h7j48ji5.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
    }

    @OnClick(R.id.btn_signup)
    public void onClickSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_login)
    public void onClickLogin() {
        pbProgressBar.setVisibility(View.VISIBLE);
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password is empty or not in correct format!", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onSignInCompleteListener)
                .addOnFailureListener(onSignInFailureListener);
    }

    @OnClick(R.id.btn_login_google)
    public void onClickLoginGoogle() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_GOOGLE_SIGNIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGNIN) {
            if (resultCode == RESULT_OK) {
                GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handeGoogleSignInResult(googleSignInResult);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: canceled");
            }
        }
    }

    private void handeGoogleSignInResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()) {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount resultSignInAccount = googleSignInResult.getSignInAccount();
            String displayName = resultSignInAccount.getDisplayName();
            String email = resultSignInAccount.getEmail();
            String id = resultSignInAccount.getId();
            String photoURL = resultSignInAccount.getPhotoUrl().toString();
            mUser = new User.Builder()
                    .setFullName(displayName)
                    .setFirstName(resultSignInAccount.getGivenName())
                    .setLastName(resultSignInAccount.getFamilyName())
                    .setAvatarURL(photoURL)
                    .setUid(id)
                    .setEmailAddress(email)
                    .build();

            firebaseAuthWithGoogle(resultSignInAccount);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUtils.getDatabaseReference()
                        .child(Constants.F_USERS)
                        .child(mFirebaseAuth.getCurrentUser().getUid())
                        .updateChildren(mUser.toHashMap())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        });
    }
}