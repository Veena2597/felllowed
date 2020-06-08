package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    ListView listView;
    ArrayList friendList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //Navigation drawer related parameter
        toolbar = findViewById(R.id.appToolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        listView = findViewById(R.id.myfriends);
        friendList = new ArrayList();
        CustomListAdapter adapter = new CustomListAdapter(this, friendList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    friendList.add(snapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        adapter.notifyDataSetChanged();
    }

    class CustomListAdapter extends BaseAdapter{
        private ArrayList<EventItem> listData;
        private LayoutInflater layoutInflater;
        public CustomListAdapter(Context aContext, ArrayList<EventItem> listData) {
            this.listData = listData;
            layoutInflater = LayoutInflater.from(aContext);
        }
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_friends, null);
            }

            FriendInfo friendInfo = (FriendInfo)getItem(position);
            TextView friend_name = convertView.findViewById(R.id.friend_name);
            ImageView friend_profile = convertView.findViewById(R.id.friend_profile);

            friend_name.setText(friendInfo.getFriendName());
            friend_profile.setImageResource(R.drawable.common_full_open_on_phone);

            convertView.setTag(friendInfo);
            return convertView;
        }
    }
    class FriendInfo{
        private String name;
        public String getFriendName() {
            return name;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.e("FSU","nav");
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.home:
                intent = new Intent(FriendsActivity.this, ForumActivity.class);
                startActivity(intent);
                break;
            case R.id.find_friends:
                intent = new Intent(FriendsActivity.this, FindUsersActivity.class);
                startActivity(intent);
                break;
            case R.id.notifcations:
                intent = new Intent(FriendsActivity.this, NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.myevents:
                intent = new Intent(FriendsActivity.this, MyEventsActivity.class);
                startActivity(intent);
                break;
            default:
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                break;
        }
        return false;
    }
}
