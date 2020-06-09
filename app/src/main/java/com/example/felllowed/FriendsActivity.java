package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class FriendsActivity extends NavActivity{

    GridView gridView;
    String[] friends;
    ArrayList profiles;
    String[] frnduid;
    private FirebaseDatabase database;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        onCreateDrawer();

        gridView = findViewById(R.id.friends_grid);

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences("FELLOWED", MODE_PRIVATE);
        friends = gson.fromJson(sharedPreferences.getString("friendslist", null), String[].class);
        frnduid = gson.fromJson(sharedPreferences.getString("friendsuidlist", null), String[].class);

        //Firebase initialization
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fellowed-a5hvee.appspot.com");
        database = FirebaseDatabase.getInstance();

        final DatabaseReference profileReference = database.getReference("Profiles");
        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profiles = new ArrayList();
                for(int i=0; i<friends.length;i++){
                    FriendItem friendItem = new FriendItem();
                    friendItem.setName(friends[i]);
                    //Log.e("Friends", frnduid[i]);
                    if(dataSnapshot.child(frnduid[i]).getValue() == null){
                        friendItem.setPictureset(1);
                    }
                    else{
                        friendItem.setPicture((String) dataSnapshot.child(frnduid[i]).getValue());
                    }
                    profiles.add(friendItem);
                }

                final FriendsActivity.CustomGridAdapter adapter = new FriendsActivity.CustomGridAdapter(FriendsActivity.this, profiles);
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
            final ImageView picture = convertView.findViewById(R.id.friend_pic);

            if(friendItem.getPictureset() == 0){
                storageReference.child(friendItem.getPicture()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(getApplicationContext()).load(uri).fit().centerCrop().into(picture);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
            else{
                picture.setImageResource(R.drawable.ic_person_black_24dp);
            }

            name.setText(friendItem.getName());

            convertView.setTag(friendItem);
            return convertView;

        }
    }

    private class FriendItem{
        String pictureUrl;
        String name;
        int pictureset = 0;

        public int getPictureset() {
            return pictureset;
        }

        public void setPictureset(int pictureset) {
            this.pictureset = pictureset;
        }

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
}
