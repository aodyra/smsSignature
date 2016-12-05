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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

        private TextView decryptDetailMessage;
        private TextView detailMessage;
        private Button buttonDecrypt;
        private Button verifiedSms;
        private EditText keyDecrypt;
        private EditText publicKeyDecrypt;

        private String textDetailMessage;
        private String textKeyDecrypt;
        private String textDecryptedMessage;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            final Sms sms = getArguments().getParcelable(ARG_OBJECT_SMS);
            View rootView = inflater.inflate(R.layout.fragment_detail_sms, container, false);
            decryptDetailMessage = (TextView) rootView.findViewById(R.id.decrypt_detail_message);
            detailMessage = (TextView) rootView.findViewById(R.id.detail_message);
            buttonDecrypt = (Button) rootView.findViewById(R.id.buttonDecrypt);
            verifiedSms = (Button) rootView.findViewById(R.id.verifiedSms);
            keyDecrypt = (EditText) rootView.findViewById(R.id.keyDecrypt);
            publicKeyDecrypt = (EditText) rootView.findViewById(R.id.publicKeyDecrypt);
            ((TextView) rootView.findViewById(R.id.detail_phone_number)).setText(sms.getPhonenumber());
            ((TextView) rootView.findViewById(R.id.detail_message)).setText(sms.getMessage());

            buttonDecrypt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textDetailMessage = detailMessage.getText().toString();
                    int positionLastMessage = textDetailMessage.indexOf("\n<ds>");
                    if(positionLastMessage != -1){
                        textDetailMessage = textDetailMessage.substring(0, positionLastMessage);
                    }
                    textKeyDecrypt = keyDecrypt.getText().toString();
                    if(!textKeyDecrypt.equals("")){
                        try {
                            IvParameterSpec iv = new IvParameterSpec("AVAVAVAVAVAVAVAV".getBytes());
                            SecretKeySpec secretKeySpec = new SecretKeySpec(Arrays.copyOf(new SHA1(textKeyDecrypt).getHash().getBytes(), 16), "AES");
                            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
                            byte[] decrypted = cipher.doFinal(Base64.decode(textDetailMessage.getBytes(),Base64.NO_WRAP));
                            textDecryptedMessage = new String(decrypted);
                            decryptDetailMessage.setText(textDecryptedMessage);
                            Toast.makeText(container.getContext(), "Message decrypted", Toast.LENGTH_LONG).show();
                        } catch (Exception x){
                            Toast.makeText(container.getContext(), x.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(container.getContext(), "Key decrypt must not empty", Toast.LENGTH_LONG).show();
                    }
                }
            });

            verifiedSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String publicKeyStr = publicKeyDecrypt.getText().toString();
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(publicKeyStr);
                    List<BigInteger> publicKeyInt= new ArrayList<>();
                    while (matcher.find() && publicKeyInt.size() <= 3) {
                        publicKeyInt.add(new BigInteger(matcher.group(0)));
                    }
                    if (publicKeyInt.size() != 2) {
                        Toast.makeText(container.getContext(), "Invalid public key", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Point publicKey = new Point(publicKeyInt.get(0), publicKeyInt.get(1));

                    String smsText = sms.getMessage();
                    pattern = Pattern.compile("\n<ds>(.+?)</ds>");
                    matcher = pattern.matcher(smsText);
                    if (!matcher.find()) {
                        Toast.makeText(container.getContext(), "Digital signature not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    smsText = smsText.replace(matcher.group(0), "");
                    Log.d("ENCRYPT", smsText);
                    pattern = Pattern.compile("\\d+");
                    matcher = pattern.matcher(matcher.group(0));
                    List<BigInteger> sigInt = new ArrayList<>();
                    while (matcher.find() && sigInt.size() <= 3) {
                        sigInt.add(new BigInteger(matcher.group(0)));
                    }
                    if (sigInt.size() != 2) {
                        Toast.makeText(container.getContext(), "Invalid digital signature", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Pair<BigInteger, BigInteger> signature = new Pair<>(sigInt.get(0), sigInt.get(1));

                    ECDSA ecdsa = new ECDSA();
                    boolean verified = ecdsa.verify(smsText, signature, publicKey);

                    Toast.makeText(container.getContext(), String.format("SMS is %sverified!", verified ? "" : "NOT "), Toast.LENGTH_LONG).show();
                }
            });
            return rootView;
        }
    }
}
