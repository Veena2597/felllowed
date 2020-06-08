package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class FriendsActivity extends ForumActivity implements NavigationView.OnNavigationItemSelectedListener {

    GridView gridView;
    ArrayList friends;
    ArrayList profiles;
    private FirebaseDatabase database;
    ForumActivity.UserData userdata;
    String currentUser;
    String frndname;
    String frnduid;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        gridView = findViewById(R.id.friends_grid);

        Intent mydata = getIntent();
        userdata = (ForumActivity.UserData) mydata.getSerializableExtra("userdata");
        Log.e("FA", String.valueOf(userdata.getFriendslist()));

        //Firebase initialization
        storageReference = FirebaseStorage.getInstance().getReference("Profiles");
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private class CustomGridAdapter extends BaseAdapter{
        private ArrayList<FriendItem> listData;
        private LayoutInflater layoutInflater;

        public CustomGridAdapter(Context aContext, ArrayList<FriendItem> listData) {
            this.listData = listData;
            layoutInflater = LayoutInflater.from(aContext);
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_friends, null);
            }
            FriendItem friendItem = (FriendItem) getItem(position);

            TextView name = convertView.findViewById(R.id.friend_name);
            ImageView picture = convertView.findViewById(R.id.friend_pic);

            name.setText(friendItem.getName());

            convertView.setTag(friendItem);
            return convertView;

        }
    }

    private class FriendItem{
        String pictureUrl;
        String name;

        public String getPicture() {
            return pictureUrl;
        }

        public void setPicture(String pictureUrl) {
            this.pictureUrl = pictureUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private class friendData{
        private Uri picture;
        private String name;
    }
}
