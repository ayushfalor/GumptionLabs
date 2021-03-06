package com.example.gumptionlabs;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class StoreActivity extends AppCompatActivity {

    FloatingActionButton fab;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference courseRef = db.collection("videos");
    private CourseAdapter adapter;
    BottomNavigationView navView;
    GoogleSignInClient mGoogleAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        setTitle("Store");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleAuth = GoogleSignIn.getClient(this,gso);
        fab=findViewById(R.id.btn_add_course);
        fab.setVisibility(View.GONE);  //ignore warning, this works
        setUpRecylerView();
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.getMenu().findItem(R.id.navigation_store).setChecked(true);
    }

    private void setUpRecylerView() {
        Query query = courseRef.whereGreaterThan("amount",0).orderBy("amount", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Course> options= new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query,Course.class)
                .build();

        adapter = new CourseAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.course_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnClickListener(new CourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //Course course = documentSnapshot.toObject(Course.class);
                String id = documentSnapshot.getId(); //document id
                //String path = documentSnapshot.getReference().getPath(); //path to doc
                Intent i = new Intent(StoreActivity.this, PurchaseCourseActivity.class);
                i.putExtra("courseId",id);
                i.putExtra("courseName",documentSnapshot.getString("name"));
                i.putExtra("courseAmount",documentSnapshot.get("amount").toString());
                i.putExtra("courseDesc",documentSnapshot.getString("description"));
                i.putExtra("courseVidCount",documentSnapshot.get("video_count").toString());
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            for (int i = 0; i < navView.getMenu().size(); i++) {
                MenuItem menuItem = navView.getMenu().getItem(i);
                boolean isChecked = menuItem.getItemId() == item.getItemId();
                menuItem.setChecked(isChecked);
            }
            switch (item.getItemId()) {
                case R.id.navigation_my_courses:{
                    startActivity(new Intent(StoreActivity.this,MyCoursesActivity.class));
                    return true;
                }
                case R.id.navigation_store: {
                    return true;
                }
                case R.id.navigation_purchase_history: {
                    startActivity(new Intent(StoreActivity.this,PurchaseHistoryActivity.class));
                    return true;
                }
                case R.id.navigation_free_courses: {
                    startActivity(new Intent(StoreActivity.this,FreeCoursesActivity.class));
                    return true;
                }

                case R.id.navigation_logout: {
                    FirebaseAuth.getInstance().signOut();
                    mGoogleAuth.signOut();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    return true;
                }
            }
            return false;
        }
    };
}
