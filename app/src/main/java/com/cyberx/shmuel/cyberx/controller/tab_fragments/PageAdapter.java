package com.cyberx.shmuel.cyberx.controller.tab_fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cyberx.shmuel.cyberx.controller.tab_fragments.TabFragments;

public class PageAdapter extends FragmentPagerAdapter {
    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return TabFragments.userListFragment;
            case 1:
                return TabFragments.chatRequestsFragment;
            case 2:
                return TabFragments.myChatFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "USERS";
            case 1:
                return "CHAT REQUESTS";
            case 2:
                return "MY CHATS";
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }
}
