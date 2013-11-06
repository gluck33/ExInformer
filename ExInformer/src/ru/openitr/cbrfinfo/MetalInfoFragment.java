
package ru.openitr.cbrfinfo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
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

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class MetalInfoFragment extends ListFragment {
    static Calendar onDate;
    public static final String LOG_TAG = "CBInfo";
    static final String INFO_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_UPDATE";
    static final Uri METAL_CONTENT_URI = MetInfoProvider.METAL_CONTENT_URI;
    private ListView metallListView;
    BroadcastReceiver br;
    CurrencyArrayAdapter ca;
    ArrayList<DragMetal> metals = new ArrayList<DragMetal>();

    protected int getLayout() {
        return R.layout.metall_prices_layout;
    }

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
        ArrayAdapter<DragMetal> metalList= new MetallArrayAdapter(getActivity(), metals);
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
        int count = lv.getHeaderViewsCount();
        TextView header  = (TextView) inflater.inflate(R.layout.header_footer, null);
        header.setText(getText(R.string.metal_page_header));
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
        Cursor c = cr.query(METAL_CONTENT_URI, MetInfoProvider.ALL_COLUMNS, null, null, MetInfoProvider.KEY_ORDER + " ASC");
        if (c.getCount() == 0) {
            FragmentActivity curActivity = getActivity();
            Intent refreshServiceIntent = new Intent(curActivity, MetalInfoFragment.class).putExtra(MainActivity.PARAM_FROM_ACTIVITY, true);
            curActivity.startService(refreshServiceIntent);
            c.close();
            c = cr.query(METAL_CONTENT_URI, MetInfoProvider.ALL_COLUMNS, null, null, MetInfoProvider.KEY_ORDER + " ASC");
        }
        if (c.moveToFirst()) {
            do {
                int code = c.getInt(MetInfoProvider.CODE_COL_NUM);
                Float  price= c.getFloat(MetInfoProvider.PRICE_COL_NUM);
                Calendar pDate = Calendar.getInstance();
                pDate.setTimeInMillis(c.getLong(MetInfoProvider.DATE_COL_NUM));
                DragMetal met = new DragMetal(code,price,pDate);
                metals.add(met);
            } while (c.moveToNext());
        }
        if (ca != null) ca.notifyDataSetChanged();
        c.close();
    }


    private class MetalInfoBroadcastReceiever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.INFO_REFRESH_INTENT)) {
                int status = intent.getIntExtra(MainActivity.PARAM_STATUS, 0);
                LogSystem.logInFile(LOG_TAG, this,"(OnRecieve) Result of service run status: " + status);
                switch (status) {
                    case (MainActivity.FIN_STATUS_OK):
                        loadMetalPricesFromProvider();
                        break;
                }
            }
        }
    }

}
