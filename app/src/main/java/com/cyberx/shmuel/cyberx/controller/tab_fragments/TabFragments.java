package com.cyberx.shmuel.cyberx.controller.tab_fragments;

import android.support.v4.view.ViewPager;

public class TabFragments {

    public static ViewPager mViewPager;

    public static PageAdapter pageAdapter;

    public static UserListFragment userListFragment;

    public static ChatRequestsFragment chatRequestsFragment;

    public static MyChatFragment myChatFragment;




    public TabFragments() {
        if (userListFragment == null) {
            userListFragment = new UserListFragment();
            chatRequestsFragment = new ChatRequestsFragment();
            myChatFragment = new MyChatFragment();

        }
    }

}
