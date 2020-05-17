package com.example.felllowed;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity{
    final String TAG = "MA";
    private ArrayList<String> eventArray;
    private ArrayAdapter adapter;

    private ListView eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        FindLocation findLocation = new FindLocation(locationManager, getApplicationContext());
        findLocation.main();
        Log.e(TAG, String.valueOf(findLocation.currentLocation));
        final String latti = String.valueOf(findLocation.currentLocation);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = database.getReference("Location").child(currentuser);
        myRef.setValue(latti);
        Log.e(TAG,currentuser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        myToolbar.inflateMenu(R.menu.options);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener () {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });


        eventArray = new ArrayList<String>();
        eventList = findViewById(R.id.eventlist);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventArray);
        eventList.setAdapter(adapter);
        eventArray.add("Find Users");
        adapter.notifyDataSetChanged();

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = position;
                Intent intent;
                switch (posID){
                    case 0:
                        intent = new Intent(MainActivity.this, FindUsersActivity.class);
                        break;

                    default:
                        intent = new Intent(MainActivity.this, MainActivity.class);
                }
                startActivity(intent);

            }
        });

        return true;
    }



    /**
     * Handler function that determines what happens when an option is pressed in the Action Bar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getApplicationContext(),login.class));
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class TagActivityPair{
        private String tag;
        private String activity;

        public TagActivityPair(String tag) {
            this.tag = tag;
        }
    }
}
