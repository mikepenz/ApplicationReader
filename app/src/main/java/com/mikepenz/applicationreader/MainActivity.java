package com.mikepenz.applicationreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.applicationreader.entity.AppInfo;
import com.mikepenz.applicationreader.items.AppInfoItem;
import com.mikepenz.applicationreader.util.UploadHelper;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FastAdapter<AppInfoItem> fastAdapter;
    private ModelAdapter<AppInfo, AppInfoItem> modelAdapter;

    private RecyclerView mRecyclerView;
    private View fabButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // preference
        final SharedPreferences pref = getSharedPreferences("com.mikepenz.applicationreader", 0);

        // Handle Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwitchDrawerItem autoUploadItem = new SwitchDrawerItem().withName(R.string.drawer_item_switch).withChecked(pref.getBoolean("autouploadenabled", false)).withOnCheckedChangeListener((drawerItem, buttonView, isChecked) -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("autouploadenabled", isChecked);
            editor.apply();
        }).withSelectable(false);

        //create the drawer and remember the `Drawer` result object
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(autoUploadItem)
                .addStickyDrawerItems(new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_github).withIdentifier(20).withSelectable(false))
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem.getIdentifier() == 20) {
                        new LibsBuilder()
                                .withFields(R.string.class.getFields())
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .start(MainActivity.this);
                    }
                    return false;
                })
                .build();

        // Fab Button
        fabButton = findViewById(R.id.fab_button);
        fabButton.setOnClickListener(fabClickListener);

        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        modelAdapter = new ModelAdapter<>(AppInfoItem::new);
        fastAdapter = FastAdapter.with(modelAdapter);
        mRecyclerView.setAdapter(fastAdapter);

        fastAdapter.withOnClickListener((v, adapter, item, position) -> {
            if (v != null) animateActivity(item.getModel(), v.findViewById(R.id.countryImage));
            return false;
        });

        new InitializeApplicationsTask().execute();
    }


    View.OnClickListener fabClickListener = view -> UploadHelper.getInstance(MainActivity.this, modelAdapter.getModels()).uploadAll();


    public void animateActivity(AppInfo appInfo, View appIcon) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("appInfo", appInfo.getComponentName());
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, androidx.core.util.Pair.create(fabButton, "fab"), androidx.core.util.Pair.create(appIcon, "appIcon"));
        startActivity(i, transitionActivityOptions.toBundle());
    }


    private class InitializeApplicationsTask extends AsyncTask<Void, List<AppInfo>, List<AppInfo>> {

        @Override
        protected void onPreExecute() {
            //Clean up ail
            modelAdapter.clear();
            super.onPreExecute();
        }

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            //Query the applications
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
            List<AppInfo> appInformation = new ArrayList<>();
            for (ResolveInfo ri : ril) {
                appInformation.add(new AppInfo(MainActivity.this, ri));
            }
            Collections.sort(appInformation);
            return appInformation;
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            modelAdapter.set(result);

            Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left);
            fadeIn.setDuration(250);
            LayoutAnimationController layoutAnimationController = new LayoutAnimationController(fadeIn);
            mRecyclerView.setLayoutAnimation(layoutAnimationController);
            mRecyclerView.startLayoutAnimation();

            super.onPostExecute(result);
        }
    }
}
