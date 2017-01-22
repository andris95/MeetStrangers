package com.soft.sanislo.meetstrangers.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

import com.google.firebase.database.DatabaseReference;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.fragment.UserListFragment;
import com.soft.sanislo.meetstrangers.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 10.10.16.
 */
public class RelationshipsActivity extends BaseActivity {
    @BindView(R.id.vp_relationships)
    ViewPager vpRelationships;

    @BindView(R.id.tl_relationships)
    TabLayout tlRelationships;

    private RelationshipsPagerAdapter mPagerAdapter;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relationships);
        ButterKnife.bind(this);
        mPagerAdapter = new RelationshipsPagerAdapter(getSupportFragmentManager());
        vpRelationships.setAdapter(mPagerAdapter);
        tlRelationships.setupWithViewPager(vpRelationships);
    }

    public static class RelationshipsPagerAdapter extends FragmentPagerAdapter {
        private static final int PAGE_COUNT = 4;
        private String tabTitles[] = new String[] { "All users", "Friends", "Followers", "Following"};

        public RelationshipsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            UserListFragment userListFragment;
            switch (position) {
                case 0:
                    userListFragment = UserListFragment.newInstance(UserListFragment.USERS_ALL);
                    break;
                case 1:
                    userListFragment = UserListFragment.newInstance(UserListFragment.USERS_FRIENDS);
                    break;
                case 2:
                    userListFragment = UserListFragment.newInstance(UserListFragment.USERS_INCOMING_REQUESTS);
                    break;
                case 3:
                    userListFragment = UserListFragment.newInstance(UserListFragment.USERS_OUTCOMING_REQUESTS);
                    break;
                default:
                    userListFragment = UserListFragment.newInstance(UserListFragment.USERS_ALL);
                    break;

            }
            return userListFragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
