package com.jeremiahespinosa.gifgallery.ui.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.adapter.ViewPagerAdapter;
import com.jeremiahespinosa.gifgallery.ui.fragments.ImagesFragment;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static String TAG = "MainActivity";
    public static final String BUNDLE_KEY = "TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();
    }

    private void setUpToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(App.getStringById(R.string.app_name));
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this);
    }

    private void setupViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        addLocalTab(adapter);

        addDropboxTab(adapter);

        addGoogleDriveTab(adapter);

        viewPager.setAdapter(adapter);

        //set up tabs after viewpager initialized
        setUpTabLayout(viewPager);
    }

    private void addLocalTab(final ViewPagerAdapter adapter){
        ImagesFragment imagesFragment = new ImagesFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY, App.getStringById(R.string.title_local));
        imagesFragment.setArguments(args);

        adapter.addFragment(imagesFragment, App.getStringById(R.string.title_local));
    }

    private void addDropboxTab(final ViewPagerAdapter adapter){
        //check if user is signed into services before filling in the tabs
        if(!PrefUtils.getPrefDropboxAccessToken().isEmpty()) {
            ImagesFragment imagesFragment = new ImagesFragment();

            Bundle args = new Bundle();
            args.putString(BUNDLE_KEY, App.getStringById(R.string.title_dropbox));
            imagesFragment.setArguments(args);

            adapter.addFragment(imagesFragment, App.getStringById(R.string.title_dropbox));
        }
    }

    private void addGoogleDriveTab(final ViewPagerAdapter adapter){
        if(!PrefUtils.getPrefDriveToken().isEmpty()) {
            ImagesFragment imagesFragment = new ImagesFragment();

            Bundle args = new Bundle();
            args.putString(BUNDLE_KEY, App.getStringById(R.string.title_drive));
            imagesFragment.setArguments(args);

            adapter.addFragment(imagesFragment, App.getStringById(R.string.title_drive));
        }
    }

    private void setUpTabLayout(ViewPager viewPager){
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //performing here helps with updating the tabs once the user has signed into a service
        setupViewPager();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                return true;
        }
        return false;
    }
}
