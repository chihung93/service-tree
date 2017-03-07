package com.zhuinden.servicetreefragmentexample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;

import com.zhuinden.servicetree.ServiceTree;
import com.zhuinden.servicetreefragmentexample.injection.ApplicationComponent;
import com.zhuinden.servicetreefragmentexample.injection.DaggerMainComponent;
import com.zhuinden.servicetreefragmentexample.injection.Injector;
import com.zhuinden.servicetreefragmentexample.injection.MainComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity
        extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private List<String> activeTags = new ArrayList<>();

    private ServiceTree serviceTree;

    class BackstackListener
            implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if(fragments == null) {
                fragments = Collections.emptyList(); // active fragments is initially NULL instead of empty list
            }
            List<String> newTags = new LinkedList<>();
            for(Fragment fragment : fragments) {
                if(fragment != null && fragment instanceof HasServices) { // active fragments is a list that can have NULL element
                    HasServices serviceFragment = ((HasServices) fragment);
                    String newTag = serviceFragment.getNodeTag();
                    newTags.add(newTag);
                    registerFragmentServices(fragment);
                }
            }
            for(String activeTag : activeTags) {
                if(!newTags.contains(activeTag)) {
                    Log.d(TAG, "Destroying [" + activeTag + "]");
                    serviceTree.removeNodeAndChildren(serviceTree.getNode(activeTag));
                }
            }
            activeTags = newTags;
        }
    }

    public void registerFragmentServices(Fragment fragment) {
        if(fragment != null && fragment instanceof HasServices) { // active fragments is a list that can have NULL element
            HasServices serviceFragment = ((HasServices) fragment);
            String newTag = serviceFragment.getNodeTag();
            if(!serviceTree.hasNodeWithKey(newTag)) {
                serviceFragment.bindServices(serviceTree.createChildNode(Nodes.getNode(TAG), newTag));
            }
        }
    }

    @BindView(R.id.root)
    RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        serviceTree = Injector.get().serviceTree();
        if(!serviceTree.hasNodeWithKey(TAG)) {
            ServiceTree.Node.Binder binder = serviceTree.createRootNode(TAG);
            ApplicationComponent applicationComponent = binder.getService(Services.DAGGER_COMPONENT);
            MainComponent mainComponent = DaggerMainComponent.builder().applicationComponent(applicationComponent).build();
            binder.bindService(Services.DAGGER_COMPONENT, mainComponent);
            mainComponent.inject(this);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getSupportFragmentManager().addOnBackStackChangedListener(new BackstackListener());
        if(savedInstanceState == null) {
            Fragment firstFragment = new FirstFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.root, firstFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return serviceTree;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    public void goToSecond() {
        getSupportFragmentManager().beginTransaction().replace(R.id.root, new SecondFragment()).addToBackStack(null).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isFinishing()) {
            serviceTree.removeNodeAndChildren(Nodes.getNode(TAG));
        }
    }

    // share activity
    @Override
    public Object getSystemService(String name) {
        if(name.equals(TAG)) {
            return this;
        }
        return super.getSystemService(name);
    }

    public static MainActivity get(Context context) {
        // noinspection ResourceType
        return (MainActivity) context.getSystemService(TAG);
    }
}

