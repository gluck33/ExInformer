
package ru.openitr.cbrfinfo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MetalInfoFragment extends ListFragment {
    static Calendar onDate;
    public static final String LOG_TAG = "CBInfo";
    static final String INFO_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_UPDATE";
    static final Uri METAL_CONTENT_URI = CBInfoProvider.METAL_CONTENT_URI;
    private ListView metallListView;
    BroadcastReceiver br;
    MetallArrayAdapter metalList;

    ArrayList<DragMetal> metals = new ArrayList<DragMetal>();
    TextView header;

    public static MetalInfoFragment newInstance(int headers, int footers) {
        MetalInfoFragment f = new MetalInfoFragment();
        Bundle args = new Bundle();
        args.putInt("headers", headers);
        args.putInt("footers", footers);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onDate = Calendar.getInstance();
        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + ":  onCreate");
        br = new MetalInfoBroadcastReceiever();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        metalList= new MetallArrayAdapter(getActivity(), metals);
        metallListView = getListView();
        Bundle args = getArguments();
        int headers = 1;
        int footers = 0;

        if (args != null){
            headers = args.getInt("headers");
            footers = args.getInt("footers");
        }

        for (int i = 0; i<headers; i++)
            addHeader(getActivity(), metallListView);
        for (int i = 0; i<footers; i++)
            addFooter(getActivity(), metallListView);
        loadMetalPricesFromProvider();
        setListAdapter(metalList);
    }

    private void addFooter(FragmentActivity activity, ListView lv) {
        LayoutInflater inflater = activity.getLayoutInflater();
        int count = lv.getHeaderViewsCount();
        TextView footer  = (TextView) inflater.inflate(R.layout.header_footer, null);
        footer.setText("Footer #" + count + 1);

        lv.addHeaderView(footer, null, false);

    }

    private void addHeader(FragmentActivity activity, ListView lv) {
        LayoutInflater inflater = activity.getLayoutInflater();
        header  = (TextView) inflater.inflate(R.layout.header_footer, null);
        header.setText(getMetalDate(getActivity()));
        lv.addHeaderView(header, null, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intF = new IntentFilter(INFO_REFRESH_INTENT);
        getActivity().registerReceiver(br, intF);
    }


    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(br);
        super.onDestroy();
        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + ": onDestroy");
    }

    /**
     * Загружает курсы валют из базы данных в массив для адаптера.
     */

    public void loadMetalPricesFromProvider() {
        metals.clear();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(METAL_CONTENT_URI, CbInfoDb.MET_ALL_COLUMNS, null, null, CbInfoDb.MET_KEY_ORDER + " ASC");
        if (c.getCount() == 0) {
            FragmentActivity curActivity = getActivity();
            Intent refreshServiceIntent = new Intent(curActivity, MetalInfoFragment.class).putExtra(MainActivity.PARAM_FROM_ACTIVITY, true);
            curActivity.startService(refreshServiceIntent);
            c.close();
            c = cr.query(METAL_CONTENT_URI, CbInfoDb.MET_ALL_COLUMNS, null, null, CbInfoDb.MET_KEY_ORDER + " ASC");
        }
        if (c.moveToFirst()) {
            do {
                int code = c.getInt(CbInfoDb.MET_CODE_COL_NUM);
                Float  price= c.getFloat(CbInfoDb.MET_PRICE_COL_NUM);
                Calendar pDate = Calendar.getInstance();
                pDate.setTimeInMillis(c.getLong(CbInfoDb.MET_DATE_COL_NUM));
                DragMetal met = new DragMetal(code,price,pDate);
                metals.add(met);
            } while (c.moveToNext());
        }
        if (metalList != null)
            metalList.notifyDataSetChanged();
            header.setText(getMetalDate(getActivity()));
        c.close();
    }

    public  static String getMetalDate(FragmentActivity activity) {
        return DragMetal.getDateInBaseAsString(activity);
    }

    private class MetalInfoBroadcastReceiever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.INFO_REFRESH_INTENT)) {
                int status = intent.getIntExtra(MainActivity.PARAM_STATUS, 0);
                LogSystem.logInFile(LOG_TAG, this,"(OnRecieve) MetalInfoBroadcastReceiever. Result of service run status: " + status);
                switch (status) {
                    case (MainActivity.FIN_STATUS_OK):
                        loadMetalPricesFromProvider();
                        break;
                }
            }
        }
    }

}
