package com.example.felllowed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class NavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    ImageView navImage;
    TextView navUsername;
    SharedPreferences sharedPreferences;
    StorageReference storageReference;
    static int ACTIVITY_ID = 0;
    static int FORUM_ID = 1;
    static int MYEVENTS_ID = 2;
    static int FRIENDS_ID = 3;
    static int NOTIFICATIONS_ID = 4;
    static int FIND_FRIENDS_ID = 5;
    static int REWARDS_ID = 6;
    static int APP_INFO = 7;


    protected void onCreateDrawer() {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_forum);
        sharedPreferences = getSharedPreferences("FELLOWED", MODE_PRIVATE);


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        toolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.open,R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        View headerView = navigationView.getHeaderView(0);
        navImage = headerView.findViewById(R.id.imageView);
        navUsername = headerView.findViewById(R.id.username_header);

        if(sharedPreferences.getString("username",null) != null){
            navUsername.setText(sharedPreferences.getString("username",null));
        }
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fellowed-a5hvee.appspot.com");
        if(sharedPreferences.getString("profilepic",null) != null){
            storageReference.child(sharedPreferences.getString("profilepic",null)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(getApplicationContext()).load(uri).fit().centerCrop().into(navImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.home:
                intent = new Intent(getApplicationContext(), ForumActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.find_friends:
                intent = new Intent(getApplicationContext(), FindUsersActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.friends:
                intent = new Intent(getApplicationContext(), FriendsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.notifcations:
                intent = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.myevents:
                intent = new Intent(getApplicationContext(), MyEventsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.rewards:
                intent = new Intent(getApplicationContext(), RewardsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.signout:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("logout",1);
                startActivity(intent);
                finish();
                break;
            case R.id.about:
                intent = new Intent(getApplicationContext(), AppInfoActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                break;
        }
        return false;
    }

    public void setNavHeader(String text){
        navUsername.setText(text);
    }

    public void setNavHeaderImage(Uri uri){
        navImage.setImageURI(uri);
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            //super.onBackPressed();
            Intent intent = new Intent(getApplicationContext(),ForumActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(ACTIVITY_ID == FORUM_ID){
            toolbar.inflateMenu(R.menu.options);
        }
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener () {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(ACTIVITY_ID == FORUM_ID && item.getItemId() == R.id.add_event_menu){
            Intent intent = new Intent(getApplicationContext(), AddEventActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
