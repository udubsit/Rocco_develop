package com.ionicframework.udubsit252887.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ionicframework.udubsit252887.BaseActivity;
import com.ionicframework.udubsit252887.R;
import com.ionicframework.udubsit252887.Utils.Constants;
import com.ionicframework.udubsit252887.Utils.Utils;
import com.ionicframework.udubsit252887.models.Users;
import com.ionicframework.udubsit252887.ui.dialogs.TermsAndConditionDialog;

public class RegisterActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {


    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        Boolean key = sp.getBoolean("onFirstStart",false);
        if(key==false)
        {
            Intent i = new Intent(this, IntroActivity.class);
            startActivity(i);
            editor.putBoolean("onFirstStart",true).apply();
            key=sp.getBoolean("onFirstStart",true);
        }

        setContentView(R.layout.activity_register);
        //Button listener
        findViewById(R.id.google_sign_in).setOnClickListener(this);
        findViewById(R.id.terms_and_conditions_textview).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in " + user.getDisplayName());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Signing in");
        progressDialog.setMessage("Authenticating your uwc account");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();
                    if (account.getEmail().contains("myuwc.ac.za") || account.getEmail().contains("uwc.ac.za")) {
                        firebaseAuthWithGoogle(account);
                    } else {
                        progressDialog.dismiss();
                        signOut();
                        Toast.makeText(RegisterActivity.this, "Not a uwc email address", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                }
            } else {
                progressDialog.dismiss();
            }
        } else {
            progressDialog.dismiss();
            Utils.showToast(this, "!RESULT_OK");
        }
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
                AuthCredential credentials = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credentials)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWIthCredential:onComplete:" + task.isSuccessful());


                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                                    Users user;
                                    String photoUrl = null;
                                    if (account.getPhotoUrl() != null) {
                                        photoUrl = account.getPhotoUrl().toString();
                                    }
                                    user = new Users(
                                            capWord(account.getDisplayName() + " " + account.getFamilyName()),
                                            account.getEmail().replace(".", ","),
                                            photoUrl,
                                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    );
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference userRef = database.getReference(Constants.USERS_KEY);
                                    userRef.child(account.getEmail().replace(".", ","))
                                            .setValue(user, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            finish();
                                        }
                                    });

                                } else {
                                    progressDialog.dismiss();
                                    Log.w(TAG, "signInWithCredential", task.getException());
                                    Log.v(TAG, "Authentification failed");
                                    Toast.makeText(RegisterActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    signOut();
                                }
                            }
                        }

                );
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in:
                progressDialog.show();
                signIn();
                break;
            case R.id.terms_and_conditions_textview:
                DialogFragment termsDialog = new TermsAndConditionDialog();
                termsDialog.show(getSupportFragmentManager(), null);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(RegisterActivity.this, "Google Play Services error.", Toast.LENGTH_LONG).show();
    }

    private String capWord(String name) {
        String temp = "";
        String[] A = name.split(" ");

        name = A[0] + " " + A[A.length - 1];
        A = name.split(" ");


        for (int i = 0; i < A.length; i++) {
            String word = A[i].toLowerCase();
            word = (word.charAt(0) + "").toUpperCase() + word.substring(1);
            temp += (word + " ");
        }
        return temp;
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}
