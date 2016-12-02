package com.example.aodyra.madavakasms;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by aodyra on 12/1/16.
 */

public class DetailListSms extends AppCompatActivity{

    public static final String ARG_SMS_LIST = "get_sms_list";

    private ArrayList<Sms> smsList;
    DetailSmsPagerAdapter mDetailSmsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_sms);

        Intent intent = getIntent();
        smsList = intent.getParcelableArrayListExtra(ARG_SMS_LIST);

        mDetailSmsPagerAdapter = new DetailSmsPagerAdapter(getSupportFragmentManager(), smsList);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDetailSmsPagerAdapter);
    }

    public static class DetailSmsPagerAdapter extends FragmentStatePagerAdapter{
        private ArrayList<Sms> smsList;

        public DetailSmsPagerAdapter(FragmentManager fm, ArrayList<Sms> smsList) {
            super(fm);
            this.smsList = smsList;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new DetailSmsFragment();
            Bundle args = new Bundle();
            args.putParcelable(DetailSmsFragment.ARG_OBJECT_SMS, smsList.get(position));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return smsList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return smsList.get(position).getPhonenumber();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if(NavUtils.shouldUpRecreateTask(this, upIntent)){
                    TaskStackBuilder.from(this)
                            .addNextIntent(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DetailSmsFragment extends Fragment {

        public static final String ARG_OBJECT_SMS = "object_sms";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Sms sms = getArguments().getParcelable(ARG_OBJECT_SMS);
            View rootView = inflater.inflate(R.layout.fragment_detail_sms, container, false);
            ((TextView) rootView.findViewById(R.id.detail_phone_number)).setText(sms.getPhonenumber());
            ((TextView) rootView.findViewById(R.id.detail_message)).setText(sms.getMessage());
            return rootView;
        }
    }
}
