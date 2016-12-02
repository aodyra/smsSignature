package com.example.aodyra.madavakasms;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String ARG_MENU_NUMBER = "MENU_NUMBER";
    private String[] mNavItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private List<Sms> smsList = new ArrayList<Sms>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mNavItems = getResources().getStringArray(R.array.nav_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mNavItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    private void prepareSmsList(){
        smsList.clear();
        Uri uriSMS = Uri.parse("content://sms/inbox");
        Cursor cur = getContentResolver().query(uriSMS,null,null,null,null);
        Sms sms;
        while(cur.moveToNext()){
            sms = new Sms(cur.getString(cur.getColumnIndex("address")),
                    cur.getString(cur.getColumnIndex("body")));
            smsList.add(sms);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void wewe(){

    }

    public void selectItem(int position){
        Fragment fragment;
        Bundle args = new Bundle();
        switch (mNavItems[position].toLowerCase()){
            case "inbox sms":
                fragment = new InboxSmsFragment();
                prepareSmsList();
                args.putParcelableArrayList(InboxSmsFragment.ARG_LIST_SMS, (ArrayList<Sms>)smsList);
                break;
            case "sent sms":
                fragment = new SentSmsFragment();
                break;
            case "generate key":
                fragment = new GenerateKeyFragment();
                break;
            default:
                fragment = new AboutFragment();
                break;

        }
        args.putInt(ARG_MENU_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(mNavItems[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public static class InboxSmsFragment extends Fragment{
        protected static final String ARG_LIST_SMS = "list_sms";

        private RecyclerView mRecyclerView;
        private RecyclerView.Adapter mRecyclerAdapter;

        public InboxSmsFragment() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_MENU_NUMBER);
            String menu = getResources().getStringArray(R.array.nav_items)[i];
            View rootView = inflater.inflate(R.layout.fragment_inbox_sms, container, false);
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            final ArrayList<Sms> smsList = getArguments().getParcelableArrayList(ARG_LIST_SMS);
            mRecyclerAdapter = new SmsAdapter(smsList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(container.getContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mRecyclerAdapter);
            mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(container.getContext(),
                    mRecyclerView, new RecyclerTouchListener.ClickListener(){
                @Override
                public void onClick(View view, int posision) {
                    Intent intent = new Intent(container.getContext(), DetailListSms.class);
                    intent.putParcelableArrayListExtra(DetailListSms.ARG_SMS_LIST, smsList);
                    startActivity(intent);
                }

                @Override
                public void onLongClick(View view, int posision) {
                }
            }));
            getActivity().setTitle(menu);
            return rootView;
        }
    }

    public static class SentSmsFragment extends Fragment{

        public static final int REQUEST_PEMISSION_SEND_SMS = 0;

        private String textPhoneNumber;
        private String textMessage;
        private String textEncryptedMessage;
        private EditText phoneNumber;
        private EditText message;

        public SentSmsFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_MENU_NUMBER);
            String menu = getResources().getStringArray(R.array.nav_items)[i];
            View rootView = inflater.inflate(R.layout.fragment_sent_sms, container, false);
            Button sendButton = (Button) rootView.findViewById(R.id.buttonSendSms);
            phoneNumber = (EditText) rootView.findViewById(R.id.input_phone_number);
            message = (EditText) rootView.findViewById(R.id.input_message);
            textPhoneNumber = phoneNumber.getText().toString();
            textMessage = phoneNumber.getText().toString();
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textPhoneNumber = phoneNumber.getText().toString();
                    textMessage = phoneNumber.getText().toString();
                    if(!textPhoneNumber.equals("") && !textMessage.equals("")){
                        sendSms();
                    } else {
                        Toast.makeText(container.getContext(), "Phone number and message must not empty",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            getActivity().setTitle(menu);
            return rootView;
        }

        protected void sendSms(){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(textPhoneNumber,null,textMessage,null,null);
            Toast.makeText(this.getContext(), "SMS sent.", Toast.LENGTH_LONG).show();
            ((MainActivity)getActivity()).selectItem(0);
        }
    }

    public static class GenerateKeyFragment extends Fragment{
        public GenerateKeyFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_MENU_NUMBER);
            String menu = getResources().getStringArray(R.array.nav_items)[i];
            View rootView = inflater.inflate(R.layout.generate_key, container, false);
            getActivity().setTitle(menu);
            return rootView;
        }
    }

    public static class AboutFragment extends Fragment{

        public AboutFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_MENU_NUMBER);
            String menu = getResources().getStringArray(R.array.nav_items)[i];
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            getActivity().setTitle(menu);
            return rootView;
        }
    }
}
