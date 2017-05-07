package com.appontherocks.soundprofile;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.appontherocks.soundprofile.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<Status> {

    private static final String TAG = "Facebook/Google Login";
    private static final int RC_SIGN_IN = 9001;
    private final int STEP_ONE_COMPLETE = 0;
    // [END declare_auth]
    List<Geofence> mGeofenceList;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth_listener]
    private DatabaseReference mDatabase;
    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClientAuth;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_ONE_COMPLETE:

                    if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }

                    if (mGeofenceList.size() > 0) {
                        LocationServices.GeofencingApi.addGeofences(
                                mGoogleApiClient,
                                getGeofencingRequest(),
                                getGeofencePendingIntent()
                        ).setResultCallback(AuthenticationActivity.this);
                    }
/*                    startActivity(new Intent(AuthenticationActivity.this, DashboardActivity.class));
                    finish();*/

                    //new saveGeoFerncesAyncTask().execute();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_authentication);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
        }
        findViewById(R.id.sign_in_button_google).setOnClickListener(this);
        findViewById(R.id.sign_out_button_google).setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        mContext = AuthenticationActivity.this;

        if ((ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.WRITE_SETTINGS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.CHANGE_CONFIGURATION)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(AuthenticationActivity.this,
                        android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
                        != PackageManager.PERMISSION_GRANTED)) {

            // Should we show an explanation?
            if ((ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.READ_CONTACTS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.WRITE_CONTACTS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.WRITE_SETTINGS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.CHANGE_CONFIGURATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.MODIFY_AUDIO_SETTINGS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(AuthenticationActivity.this,
                            android.Manifest.permission.RECEIVE_BOOT_COMPLETED))) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(AuthenticationActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_SETTINGS, android.Manifest.permission.CHANGE_CONFIGURATION, android.Manifest.permission.MODIFY_AUDIO_SETTINGS, android.Manifest.permission.RECEIVE_BOOT_COMPLETED},
                        6);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        mGeofenceList = new ArrayList<Geofence>();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //fetchGeoFences();
                    Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
        ((AppCompatButton) findViewById(R.id.button_facebook)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });
        // [END initialize_fblogin]

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClientAuth = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        ((AppCompatButton) findViewById(R.id.button_google)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
    }

    private void fetchGeoFences() {
        Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        profileArrayList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            SoundProfile profile = ds.getValue(SoundProfile.class);
                            if ((profile.mKey != null) && (profile.latitude != null) && (profile.longitude != null) && !((profile.latitude + "").equals("")) && !((profile.longitude + "").equals(""))) {
                                mGeofenceList.add(new Geofence.Builder()
                                        // Set the request ID of the geofence. This is a string to identify this
                                        // geofence.
                                        .setRequestId(profile.mKey)
                                        .setCircularRegion(
                                                Double.parseDouble(profile.latitude),
                                                Double.parseDouble(profile.longitude),
                                                50
                                        )
                                        .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_TIME)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                                Geofence.GEOFENCE_TRANSITION_EXIT)
                                        .build());
                            }
                        }
                        Message msg = Message.obtain();
                        msg.what = STEP_ONE_COMPLETE;
                        handler.sendMessage(msg);
                        FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        backgroundThread.start();

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);


        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
/*        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }*/
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_start_add_listener]

    // [START on_activity_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END on_stop_remove_listener]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        }
                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END on_activity_result]

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    public void signOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();

        updateUI(null);
    }
    // [END auth_with_facebook]

    // [START signin]
    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClientAuth);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOutGoogle() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClientAuth).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }
    // [END signin]

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClientAuth).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_facebook_signout) {
            signOut();
        } else if (i == R.id.sign_in_button_google) {
            signInGoogle();
        } else if (i == R.id.sign_out_button_google) {
            signOutGoogle();
        } else if (i == R.id.disconnect_button) {
            revokeAccess();
        }
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        fetchGeoFences();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    // [END basic_write]

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public class saveGeoFerncesAyncTask extends AsyncTask<Void, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(AuthenticationActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Go to MainActivity
            startActivity(new Intent(AuthenticationActivity.this, DashboardActivity.class));
            finish();
        }
    }
}
