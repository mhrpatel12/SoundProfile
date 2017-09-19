package com.appontherocks.soundprofile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.adapter.ViewPagerAdapter;
import com.appontherocks.soundprofile.fragments.AdvancedSettingsFragment;
import com.appontherocks.soundprofile.fragments.DashboardFragment;
import com.appontherocks.soundprofile.fragments.DefaultProfileFragment;
import com.appontherocks.soundprofile.fragments.ProfilesFragment;
import com.appontherocks.soundprofile.fragments.SleepyHoursFragment;


public class HomeActivity extends BaseActivity {

    private ViewPager mViewPager;
    private BottomNavigationView navigation;
    MenuItem prevMenuItem;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_dashboard:
                    mViewPager.setCurrentItem(0);
                    return true;
                case R.id.nav_profiles:
                    mViewPager.setCurrentItem(1);
                    return true;
                case R.id.nav_default_profile:
                    mViewPager.setCurrentItem(2);
                    return true;
                case R.id.nav_sleep_hours:
                    mViewPager.setCurrentItem(3);
                    return true;
                case R.id.nav_advanced_settings:
                    mViewPager.setCurrentItem(4);
                    return true;
            }
            return false;
        }

    };

    private void initFragments() {
        DashboardFragment dashboardFragment = new DashboardFragment();
        ProfilesFragment profilesFragment = new ProfilesFragment();
        DefaultProfileFragment defaultProfileFragment = new DefaultProfileFragment();
        SleepyHoursFragment sleepyHoursFragment = new SleepyHoursFragment();
        AdvancedSettingsFragment advancedSettingsFragment = new AdvancedSettingsFragment();

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager_bottom_navigation);
        pagerAdapter.addFragment(dashboardFragment);
        pagerAdapter.addFragment(profilesFragment);
        pagerAdapter.addFragment(defaultProfileFragment);
        pagerAdapter.addFragment(sleepyHoursFragment);
        pagerAdapter.addFragment(advancedSettingsFragment);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(pageChangeListener);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // If BottomNavigationView has more than 3 items, using reflection to disable shift mode
        //BottomNavigationViewHelper.disableShiftMode(navigation);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);
            } else {
                navigation.getMenu().getItem(0).setChecked(false);
            }
            Log.d("page", "onPageSelected: " + position);
            navigation.getMenu().getItem(position).setChecked(true);
            prevMenuItem = navigation.getMenu().getItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initFragments();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.promps_double_back), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
