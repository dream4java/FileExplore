
package com.shadow.activity;

import java.util.ArrayList;

import net.micode.fileexplorer.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;

import com.shadow.util.Util;
/**
 * Main activity
 * @author dream4java
 * 2015-11-01
 */
public class FileExplorerTabActivity extends Activity 
{
    private static final String INSTANCESTATE_TAB 		= "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES 	= 2;
    ViewPager mViewPager								= null;
    TabsAdapter mTabsAdapter							= null;
    ActionMode mActionMode								= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);
        initView();
    }

    /**
     * 
     * @return
     */
    private void initView()
    {
    	mViewPager 			= (ViewPager) findViewById(R.id.pager);
    	mTabsAdapter		= new TabsAdapter(this, mViewPager);
        ActionBar bar		= getActionBar();
       
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_category),
                FileCategoryActivity.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_sd),
                FileViewActivity.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_remote),
                ServerControlActivity.class, null);
        
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        bar.setSelectedNavigationItem(PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(INSTANCESTATE_TAB, Util.CATEGORY_TAB_INDEX));
    }
    
    public void reInstantiateCategoryTab()
    {
        mTabsAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX,
                mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mTabsAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }
    
    public interface IBackPressedListener 
    {
        /**
         * 处理back事件。
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }

    public void setActionMode(ActionMode actionMode) 
    {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() 
    {
        return mActionMode;
    }

    public Fragment getFragment(int tabIndex) {
        return mTabsAdapter.getItem(tabIndex);
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(INSTANCESTATE_TAB, getActionBar().getSelectedNavigationIndex());
        editor.commit();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        if (getActionBar().getSelectedNavigationIndex() == Util.CATEGORY_TAB_INDEX)
        {
            FileCategoryActivity categoryFragement = (FileCategoryActivity) mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) 
            {
                reInstantiateCategoryTab();
            } 
            else
            {
                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed()
    {
        IBackPressedListener backPressedListener = (IBackPressedListener) mTabsAdapter
                .getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    /**
     *  tab adapter
     * @author dream4java
     *
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener
            {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) 
        {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args)
        {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount()
        {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) 
        {
            TabInfo info = mTabs.get(position);
            if (info.fragment == null) 
            {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
        }

        @Override
        public void onPageSelected(int position) 
        {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) 
        {
            Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) 
            {
                if (mTabs.get(i) == tag) 
                {
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
            if(!tab.getText().equals(mContext.getString(R.string.tab_sd)))
            {
                ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
                if (actionMode != null)
                {
                    actionMode.finish();
                }
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) 
        {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
        }
    }
}
