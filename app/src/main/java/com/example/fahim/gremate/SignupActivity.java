package com.example.fahim.gremate;


import android.app.Activity;
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
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private AppCompatButton signup;
    private TextView failText;
    private ProgressBar suSpinner;
    private EditText name, email, password;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("UserData");

        suSpinner = (ProgressBar) findViewById(R.id.suSpinner);
        suSpinner.setVisibility(View.GONE);

        name = (EditText) findViewById(R.id.suUserName);
        email = (EditText) findViewById(R.id.suUserEmail);
        password = (EditText) findViewById(R.id.suPassword);

        password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard(SignupActivity.this);
                    signup.performClick();
                    return true;
                }
                return false;
            }
        });

        setTitle(Html.fromHtml("<font color='#BDCBDA'>GREMate</font>"));

        failText = (TextView) findViewById(R.id.suFailText);
        failText.setVisibility(View.GONE);

        signup = (AppCompatButton) findViewById(R.id.suSignup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(SignupActivity.this);

                suSpinner.setVisibility(View.VISIBLE);
                failText.setVisibility(View.GONE);

                final String uname = name.getText().toString();
                final String uemail = email.getText().toString();
                String upass = password.getText().toString();

                if(uname.isEmpty() || uemail.isEmpty() || upass.isEmpty() ) {
                    failText.setText("Text fields must not be empty!");
                    failText.setVisibility(View.VISIBLE);
                    suSpinner.setVisibility(View.GONE);
                    return;
                }

                if(upass.length() < 6 ) {
                    failText.setText("Password must be atleast 6 characters long!");
                    failText.setVisibility(View.VISIBLE);
                    suSpinner.setVisibility(View.GONE);
                    return;
                }

                auth.createUserWithEmailAndPassword(uemail, upass).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            failText.setText("Sign up failed! Please try again.!");
                            failText.setVisibility(View.VISIBLE);
                            suSpinner.setVisibility(View.GONE);
                        }
                        else{
                            UserData u = new UserData(uname, uemail);
                            String uid = task.getResult().getUser().getUid();
                            ref.child(task.getResult().getUser().getUid()).setValue(u);
                            Toast.makeText(SignupActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();
                            DB.initNewUser(uid, getApplicationContext());
                            SignupActivity.this.finish();
                        }
                    }
                });
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
