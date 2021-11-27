package com.example.fahim.gremate;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private AppCompatButton login, signup;
    private ProgressBar loginSpinner;
    private TextView loginFail;
    private EditText email, password;

    private DatabaseReference ref;

    private static boolean persist = false;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        if(!persist) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().setPersistenceCacheSizeBytes(26214400);
            persist = true;
        }

        if(auth.getCurrentUser()!=null){
//            DB.move_temp();

            Intent intent = new Intent(LoginActivity.this, WordSetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            LoginActivity.this.finish();
        }

        loginSpinner = (ProgressBar)findViewById(R.id.loginSpinner);
        loginSpinner.setVisibility(View.GONE);

        loginFail = (TextView) findViewById(R.id.loginFail);
        loginFail.setVisibility(View.GONE);

        email = (EditText)findViewById(R.id.userEmail);
        password = (EditText)findViewById(R.id.password);

        password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard(LoginActivity.this);
                    login.performClick();
                    return true;
                }
                return false;
            }
        });

        setTitle(Html.fromHtml("<font color='#BDCBDA'>GREMate</font>"));

        login = (AppCompatButton)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(LoginActivity.this);

                String uemail = email.getText().toString();
                String pword = password.getText().toString();

                if(uemail.length()<1 || pword.length()<1) return;

                loginSpinner.setVisibility(View.VISIBLE);
                loginFail.setVisibility(View.GONE);

                auth.signInWithEmailAndPassword(uemail, pword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            ref = FirebaseDatabase.getInstance().getReference().child("UserWords").child(userId);
                            ref.keepSynced(true);

                            Intent intent = new Intent(LoginActivity.this, WordSetActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(intent);
                            LoginActivity.this.finish();
                        }
                        else {
                            loginFail.setVisibility(View.VISIBLE);
                            loginSpinner.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        signup = (AppCompatButton)findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
//                startActivity(i);
            }
        });

    }

    void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
