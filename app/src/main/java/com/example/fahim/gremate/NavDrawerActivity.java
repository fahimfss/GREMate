package com.example.fahim.gremate;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NavDrawerActivity extends AppCompatActivity {

    protected DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    protected ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    protected String userName;
    FrameLayout actContent;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {

        context = this;

        mDrawerLayout= (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_nav_drawer, null);
        actContent= (FrameLayout) mDrawerLayout.findViewById(R.id.frame_container);

        getLayoutInflater().inflate(layoutResID, actContent, true);
        super.setContentView(mDrawerLayout);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setName();
    }

    private void setName(){
        DBRef db = new DBRef();
        DatabaseReference mRef = db.userDataRef();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        UserData user = dataSnapshot.getValue(UserData.class);
                        ((TextView) findViewById(R.id.menu_username)).setText(user.getUserName());
                        ((TextView) findViewById(R.id.menu_username)).setTextColor(getResources().getColor(R.color.darkFore4));
                        userName = user.getUserName();
                    }
                }
                catch (Exception e){
                    userName = "";
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.darkFore4));
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
