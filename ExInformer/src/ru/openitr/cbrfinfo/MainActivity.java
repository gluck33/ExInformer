package ru.openitr.cbrfinfo;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class MainActivity extends FragmentActivity {
    public static final int CURRENCY_FRAGMENT = 0;
    public static final int METALL_FRAGMENT = 1;
    public static final int NEWS_FRAGMENT = 3;
    public static final int FRAGMENTS = 1;
    static final String INFO_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_UPDATE";
    static final String INFO_NEED_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_NEED_UPDATE";
    public static final int NOTIFICATION_ID = 1;
    public static final String PARAM_STATUS = "status";
    public static final int STATUS_BEGIN_REFRESH = 10;
    public static final int FIN_STATUS_OK = 20;
    public static final int FINS_STATUS_NETWORK_DISABLE = 30;
    public static final int FIN_STATUS_NOT_RESPOND = 40;
    public static final int FIN_STATUS_NO_DATA = 50;
    public static final int GET_INFO = 80;
    private static final int SHOW_PREFERENCES = 1;
    public static final String PARAM_DATE = "date";
    public static final String PARAM_ONLY_SET_ALARM = "only_set";
    public static final String PARAM_FROM_ACTIVITY = "from_activity";

    static final Uri CURRENCYS_URI = Uri.parse("content://ru.openitr.cbrfinfo.currency/currencys");
    boolean OldAPIVersion;
    static Calendar onDate;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private final List<ListFragment> fragments = new ArrayList<ListFragment>();
    private ViewPager viewPager;
    NotificationManager notificationManager;
    BroadcastReceiver br;
    Intent refreshServiceIntent;
    boolean onDateSet;

    //****************************
    private View mContentView;
    private View mLoadingView;
    private int mShortAnimationDuration;

    //****************************
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OldAPIVersion = Build.VERSION.SDK_INT >= 11 ? false : requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        mContentView = findViewById(R.id.pager);
        mLoadingView = findViewById(R.id.loading_spinner);
        mLoadingView.setVisibility(View.GONE);
        onDate = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        fragments.add(CURRENCY_FRAGMENT, new CurrencyInfoFragment());
        br = new MainActivityBroadcastReceiever();
        refreshServiceIntent = new Intent(this, InfoRefreshService.class);
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return FRAGMENTS;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                String[] pagesTitles;
                pagesTitles = getResources().getStringArray(R.array.page_titles);
                return pagesTitles[position];
            }
        };
        //******************************
        //*****************************
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(CURRENCY_FRAGMENT);
        setInfoDateToTitle();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intF = new IntentFilter(INFO_REFRESH_INTENT);
        registerReceiver(br, intF);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(br);
        super.onPause();
    }



    /**
     * Вывод даты курса в заголовок.
     */



    private void customTitleBar(String left) {
        if (OldAPIVersion) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.apptitle);
            TextView titleLeft = (TextView) findViewById(R.id.titleLeft);
            titleLeft.setText(left);
//            setDateOnTitle(onDate);
        }
    }

    private void setDateOnTitle(Calendar onDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String stringDate = sdf.format(onDate.getTime());
        if (OldAPIVersion) {
            customTitleBar(getText(R.string.app_name).toString());
            TextView titleTvRight = (TextView) findViewById(R.id.titleRight);
            titleTvRight.setText(getText(R.string.appTitleDatePrefix).toString() + ": " + stringDate);
        } else {
            ActionBar bar = getActionBar();
            bar.setSubtitle(getString(R.string.appTitleDatePrefix) + ": " + stringDate);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_PREFERENCES)
            if (resultCode == Activity.RESULT_OK) {
                refreshPreferences();
            }
    }




    public void setInfoDateToTitle() {

        Cursor cursor = (getContentResolver().query(CURRENCYS_URI, new String[]{CurrencyDbAdapter.KEY_DATE}, null, null, null));
        if (cursor.moveToFirst()) {
            onDate.setTimeInMillis(cursor.getLong(0));
            setDateOnTitle(onDate);
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (OldAPIVersion) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        } else {
            getMenuInflater().inflate(R.menu.root_menu, menu);
            return true;
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.setDataItem):
                onDateSet = false;
                showDatePicker();
                return true;
            case (R.id.settingsItem):
                Intent i = new Intent(this, BasePreferencesActivity.class);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            case (R.id.root_menu):
                showMenu(findViewById(R.id.root_menu));
                return true;
            case (R.id.refreshItem):
                getInfo(0);
                return true;
        }
        return false;
    }


    public void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onOptionsItemSelected(item);
                return false;
            }
        });
        popupMenu.inflate(R.menu.main_menu);
        popupMenu.show();

    }

    private void refreshPreferences() {
        refreshServiceIntent.putExtra(PARAM_ONLY_SET_ALARM, true);
        startService(refreshServiceIntent);
    }


    private void getInfo(Calendar newDate) {
        onDate = newDate;
        refreshServiceIntent.putExtra(PARAM_DATE, newDate.getTimeInMillis());
        startService(refreshServiceIntent);
    }

    private void getInfo(long timeInMillis) {
        refreshServiceIntent.putExtra(PARAM_DATE, timeInMillis);
        if (timeInMillis == 0) refreshServiceIntent.putExtra(PARAM_FROM_ACTIVITY, true);
       startService(refreshServiceIntent);

    }

    private void showDatePicker() {
        AppDialog date = AppDialog.newInstance(AppDialog.DATE_DIALOG);
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(ondate);
        date.show(getSupportFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            if (onDateSet){
                return;
            }
            onDateSet = true;
            onDate.set(Calendar.YEAR, year);
            onDate.set(Calendar.MONTH, monthOfYear);
            onDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            getInfo(onDate);
        }
    };

    private void beginProgress(){
        mContentView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mShortAnimationDuration = getResources().getInteger(
        android.R.integer.config_shortAnimTime);
    }

    private void endProgress() {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        mContentView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        mLoadingView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingView.setVisibility(View.GONE);
                    }
                });
    }

    /**
     *
     */

    public class MainActivityBroadcastReceiever extends BroadcastReceiver {
        public static final String LOG_TAG = "CBInfo";
        AppDialog progressDialog;
        AppDialog notRespondDialog;
        AppDialog netSettingsDialog;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.INFO_REFRESH_INTENT)) {
                int status = intent.getIntExtra(MainActivity.PARAM_STATUS, 0);
                LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + ": (OnRecieve) Result of service run status: " + status);
                switch (status) {
                    case STATUS_BEGIN_REFRESH:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Begin updating info.");
//                        progressDialog = AppDialog.newInstance(AppDialog.PROGRESS_DIALOG);
//                        progressDialog.show(getSupportFragmentManager(), "");
                        beginProgress();
                        break;
                    case FIN_STATUS_NO_DATA:
                        progressDialog.dismiss();
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : No data receive.");

                        break;
                    case FIN_STATUS_NOT_RESPOND:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Server not respond.");
                        progressDialog.dismiss();
                        notRespondDialog = AppDialog.newInstance(AppDialog.NOT_RESPOND_DIALOG);
                        notRespondDialog.setNotRespondPositiveOnClick(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                getInfo(0);
                            }
                        });
                        notRespondDialog.show(getSupportFragmentManager(), Integer.toString(AppDialog.NOT_RESPOND_DIALOG));
                        break;
                    case FINS_STATUS_NETWORK_DISABLE:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Network disabled.");
                        if (progressDialog != null)
                            progressDialog.dismiss();

                        netSettingsDialog = AppDialog.newInstance(AppDialog.NETSETTINGS_DIALOG);
                        netSettingsDialog.show(getSupportFragmentManager(),Integer.toString(AppDialog.NETSETTINGS_DIALOG));
                        break;
                    default:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Refreshing OK.");
                        setInfoDateToTitle();
                        endProgress();
//                        if (progressDialog != null)
//                            progressDialog.dismiss();
                        break;
                }
            }
            if (intent.getAction().equals(MainActivity.INFO_NEED_REFRESH_INTENT)) {
                getInfo(0);
            }
        }
    }

}
