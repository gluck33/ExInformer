package ru.openitr.cbrfinfo;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.DatePicker;
//import android.widget.PopupMenu;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;


public class MainActivity extends ActionBarActivity {
    public static final int CURRENCY_FRAGMENT = 0;
    public static final int METALL_FRAGMENT = 1;
    public static final int NEWS_FRAGMENT = 3;
    public static final int FRAGMENTS = 2;
    static final String INFO_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_UPDATE";
    static final String INFO_NEED_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_NEED_UPDATE";
    public static final int NOTIFICATION_ID = 1;
    public static final String PARAM_STATUS = "status";
    public static final int STATUS_BEGIN_REFRESH = 10;
    public static final int FIN_STATUS_OK = 20;
    public static final int FIN_STATUS_NETWORK_DISABLE = 30;
    public static final int FIN_STATUS_NOT_RESPOND = 40;
    public static final int FIN_STATUS_NO_DATA = 50;
    private static final int FIN_STATUS_BAD_DATA = 70;
    public static final int GET_INFO = 80;
    private static final int SHOW_PREFERENCES = 1;
    public static final String PARAM_DATE = "date";
    public static final String PARAM_ONLY_SET_ALARM = "only_set";
    public static final String PARAM_FROM_ACTIVITY = "from_activity";

    static Calendar onDate;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private final List<ListFragment> fragments = new ArrayList<ListFragment>();
    private ViewPager viewPager;
    public boolean firstRun;
    public boolean infoLoaded [] = {false, false};
    NotificationManager notificationManager;
    BroadcastReceiver br;
    Intent refreshServiceIntent;
    Intent refreshMetInfoService;
    boolean onDateSet;
    private View mContentView;
    private View mLoadingView;
    AlphaAnimation alpha;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentView = findViewById(R.id.pager);
        mLoadingView = findViewById(R.id.loading_spinner);
        mLoadingView.setVisibility(View.GONE);
        onDate = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        fragments.add(CURRENCY_FRAGMENT, new CurrencyInfoFragment());
        fragments.add(METALL_FRAGMENT, new MetalInfoFragment());
        br = new MainActivityBroadcastReceiever();
        refreshServiceIntent = new Intent(this, CurInfoRefreshService.class);
        refreshMetInfoService = new Intent(this, MetInfoRefreshService.class);
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



            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
            }
        };
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //LogSystem.logInFile("CBInfo", "onPageScrolled");
            }

            @Override
            public void onPageSelected(int i) {
                initInfo(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
               // LogSystem.logInFile("CBInfo", "onPageScrollStateChanged");
            }

            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }
        });
        viewPager.setCurrentItem(CURRENCY_FRAGMENT);
        initInfo(0);
// setInfoDateToTitle();

    }

    public void initInfo(int i){
        if (isFirstRun(this, i)){
            getInfo(0);
            LogSystem.logInFile("CBInfo", this, "initInfo");
        }
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_PREFERENCES)
            if (resultCode == Activity.RESULT_OK) {
                refreshPreferences();
            }
    }




    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.root_menu, menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.root_menu, menu);
        return super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.root_menu, menu);
//        return true;
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
            case (R.id.exiItem):
                finish();
                return true;
            case 100:
                if (LogSystem.DEBUG){
                    TestFunc.rollbackToYesterday(getApplicationContext());
                    CurrencyInfoFragment curf = (CurrencyInfoFragment) fragments.get(CURRENCY_FRAGMENT);
                    curf.loadCurrencysFromProvider();
                    MetalInfoFragment mf = (MetalInfoFragment) fragments.get(METALL_FRAGMENT);
                    mf.loadMetalPricesFromProvider();
                }
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
        if (LogSystem.DEBUG)
            popupMenu.getMenu().add(Menu.NONE, 100, 500, "Возврат на вчера");
        popupMenu.inflate(R.menu.main_menu);
        popupMenu.show();

    }

    private void refreshPreferences() {
        refreshServiceIntent.putExtra(PARAM_ONLY_SET_ALARM, true);
        refreshMetInfoService.putExtra(PARAM_ONLY_SET_ALARM, true);
        startService(refreshServiceIntent);
        startService(refreshMetInfoService);
    }


    public void getInfo(Calendar newDate) {
        onDate = newDate;
        getInfo(newDate.getTimeInMillis());
    }

    public void getInfo(long timeInMillis) {
        if (timeInMillis == 0) refreshServiceIntent.putExtra(PARAM_FROM_ACTIVITY, true);
            switch (viewPager.getCurrentItem()){
                case (CURRENCY_FRAGMENT):
                    refreshServiceIntent.putExtra(PARAM_FROM_ACTIVITY, true);
                    refreshServiceIntent.putExtra(PARAM_DATE, timeInMillis);
                    startService(refreshServiceIntent);
                    infoLoaded[CURRENCY_FRAGMENT] = true;
                    break;
                case (METALL_FRAGMENT):
                    refreshMetInfoService.putExtra(PARAM_FROM_ACTIVITY, true);
                    refreshMetInfoService.putExtra(PARAM_DATE, timeInMillis);
                    startService(refreshMetInfoService);
                    infoLoaded[METALL_FRAGMENT] = true;
                    break;
            }
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
        alpha = new AlphaAnimation(0.5F, 0.5F);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLoadingView.setVisibility(View.GONE);
                mContentView.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        alpha.setDuration(60*1000);
        alpha.setFillAfter(false);
        mLoadingView.setVisibility(View.VISIBLE);
        mContentView.startAnimation(alpha);
    }

    private void endProgress() {
        if (alpha != null)  alpha.cancel();
    }


    private boolean isFirstRun(Context context, int page){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean firstRun = sp.getBoolean("PREF_FIRST_RUN"+page, true);
            if (firstRun) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("PREF_FIRST_RUN"+page, false);
                editor.commit();
            }
        return firstRun;
    }

    /**
     *
     */

    public class MainActivityBroadcastReceiever extends BroadcastReceiver {
        public static final String LOG_TAG = "CBInfo";
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
                        beginProgress();
                        break;
                    case FIN_STATUS_NO_DATA:
                        endProgress();
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : No data receive.");
                        break;
                    case FIN_STATUS_BAD_DATA:

                    case FIN_STATUS_NOT_RESPOND:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Server not respond.");
                        endProgress();
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
                    case FIN_STATUS_NETWORK_DISABLE:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Network disabled.");
                        endProgress();
                        netSettingsDialog = AppDialog.newInstance(AppDialog.NETSETTINGS_DIALOG);
                        netSettingsDialog.show(getSupportFragmentManager(),Integer.toString(AppDialog.NETSETTINGS_DIALOG));
                        break;
                    default:
                        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + " : Refreshing OK.");
                        endProgress();
                        break;
                }
            }
            if (intent.getAction().equals(MainActivity.INFO_NEED_REFRESH_INTENT)) {
                getInfo(0);
            }
        }
    }

}
