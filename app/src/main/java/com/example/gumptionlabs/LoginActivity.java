package com.example.gumptionlabs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mAuth;
    private String imei,imei1;
    EditText inEmailEt, inPasswordEt;
    //   ProgressBar inPbar;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleAuth;
    int RC_SIGNIN = 0;
    GoogleApiClient mGoogleApiClient;
    private FirebaseFirestore db;
    //TextView username_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        inEmailEt = findViewById(R.id.inEmail);
        inPasswordEt = findViewById(R.id.inPassword);
        //inPbar = findViewById(R.id.inProgressBar);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.inSignup).setOnClickListener(this);
        findViewById(R.id.inSignin).setOnClickListener(this);
        findViewById(R.id.inForgot).setOnClickListener(this);
        findViewById(R.id.gSignIn).setOnClickListener(this);
        // username_header=findViewById(R.id.uname_header);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mGoogleAuth = GoogleSignIn.getClient(this,gso);
        db=FirebaseFirestore.getInstance();
    }

    private void gsignIn()
    {
        Intent signInIntent=Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGNIN);

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        /*super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==RC_SIGNIN){
            //GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
           // GoogleSignInAccount account = null;
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
           // firebaseAuthWithGoogle(account);
           // handleSignInResult(result);
        }*/

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGNIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    public void handleSignInResult(GoogleSignInResult result)
    {
        if(result.isSuccess())
        {
            GoogleSignInAccount acct=result.getSignInAccount();
            startActivity(new Intent(this,homeActivity.class));
            //username_header.setText(acct.getDisplayName());

            //Toast.makeText(this, "hello" +acct.getDisplayName(), Toast.LENGTH_SHORT).show();
        }
        else {

        }
    }

    private void userLogin() {
        String email = inEmailEt.getText().toString();
        String password = inPasswordEt.getText().toString();

        if (email.isEmpty()) {
            inEmailEt.setError("Email is required");
            inEmailEt.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inEmailEt.setError("Please enter a valid email");
            inEmailEt.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            inPasswordEt.setError("Password is required");
            inPasswordEt.requestFocus();
            return;
        }

        if (password.length() < 6) {
            inPasswordEt.setError("Password must contain at least 6 characters");
            inPasswordEt.requestFocus();
            return;
        }

        //inPbar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // inPbar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                user = mAuth.getCurrentUser();
                //check current imei
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                if(telephonyManager!=null)
                    imei=telephonyManager.getDeviceId().trim(); //permission requested in splash
                else
                {
                    imei="error";
                }
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference docRef =  db.collection("users").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                imei1 = document.getString("imei").trim();
                            } else {
                                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                imei1 = "error";
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Can't fetch data", Toast.LENGTH_SHORT).show();
                            imei1 = "Error";
                        }
                        //Toast.makeText(LoginActivity.this, "Original IMEI = " + imei1 + ", Current IMEI = " + imei, Toast.LENGTH_LONG).show();
                        if (imei.equals(imei1)) {
                            Toast.makeText(LoginActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                            // FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                            startActivity(new Intent(getApplicationContext(), homeActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Sorry, you can only access the app using 1 device", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            //dont let into app with different imei
                        }
                    }
                    });
                }

                 else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if(mAuth.getCurrentUser() != null || acc!=null)  //dont log in again if user is already signed in
        {
            finish();
            startActivity(new Intent(this,homeActivity.class));
       // }
    }

    /*private void gLogin()
    {
        Intent gIntent = mGoogleAuth.getSignInIntent();
        startActivityForResult(gIntent, RC_SIGNIN);
       // Toast.makeText(this, "hnhjg", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGNIN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount acc= completedTask.getResult(ApiException.class);
            startActivity(new Intent(this, HomePageActivityhome.class));
        } catch (ApiException e){
            Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
        }
   */ }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            boolean newuser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (newuser) {
                                Toast.makeText(LoginActivity.this, "Welcome to Gumption Labs", Toast.LENGTH_SHORT).show();
                                //FirebaseUser user = mAuth.getCurrentUser();
                                finish();
                                startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                            }
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("TAG", "signInWithCredential:success");
                            else {
                                //check current imei
                                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                if (telephonyManager != null)
                                    imei = telephonyManager.getDeviceId().trim(); //permission requested in splash, ignore this error
                                else {
                                    imei = "error";
                                }
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DocumentReference docRef =  db.collection("users").document(uid);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document != null && document.exists()) {
                                                imei1 = document.getString("imei").trim();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                imei1 = "error";
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Can't fetch data", Toast.LENGTH_SHORT).show();
                                            imei1 = "Error";
                                        }
                                        //Toast.makeText(LoginActivity.this, "Original IMEI = " + imei1 + ", Current IMEI = " + imei, Toast.LENGTH_LONG).show();
                                        if (imei.equals(imei1)) {
                                            Toast.makeText(LoginActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                                            // FirebaseUser user = mAuth.getCurrentUser();
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), homeActivity.class));
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Sorry, you can only access the app using 1 device", Toast.LENGTH_LONG).show();
                                            FirebaseAuth.getInstance().signOut();
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                            //dont let into app with different imei
                                        }
                                    }
                                });



                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inSignup:
                finish();
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                break;

            case R.id.inSignin:
                userLogin();
                break;

            case R.id.inForgot:
                startActivity(new Intent(getApplicationContext(),PasswordActivity.class));
                break;

            case R.id.gSignIn:
                gsignIn();
                break;

        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }


}