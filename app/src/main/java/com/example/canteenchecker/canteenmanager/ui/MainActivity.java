package com.example.canteenchecker.canteenmanager.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.canteenchecker.canteenmanager.CanteenManagerApplication;
import com.example.canteenchecker.canteenmanager.R;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private final String TAG = MainActivity.class.toString();

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        
                        int id = menuItem.getItemId();

                        switch (id) {
                            case R.id.nav_editCanteen:
                                MainActivity.this.getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.content_frame,
                                                CanteenFragment.create())
                                        .addToBackStack(null)
                                        .commit();
                                return true;

                            case R.id.nav_manageRatings:
                                String canteenId = CanteenManagerApplication.getInstance()
                                        .getCanteenId();

                                MainActivity.this.getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.content_frame,
                                                RatingsFragment.create(canteenId))
                                        .addToBackStack(null)
                                        .commit();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

        // set standard fragment
        MainActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame,
                        CanteenFragment.create())
                .addToBackStack(null)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.mniLogout:
                CanteenManagerApplication.getInstance().logOut();
                startActivity(LoginActivity.createIntent(MainActivity.this));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.mniLogout)
                .setVisible(CanteenManagerApplication.getInstance().isAuthenticated());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}