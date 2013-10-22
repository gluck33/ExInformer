
package ru.openitr.cbrfinfo;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class CurrencyInfoFragment extends ListFragment {
    static Calendar onDate;
    public static final String LOG_TAG = "CBInfo";
    public static final int STATUS_BEGIN_REFRESH = 10;
    public static final int FIN_STATUS_OK = 20;
    public static final int FIN_STATUS_NOT_RESPOND = 40;
    public static final int FIN_STATUS_NO_DATA = 50;
    public static final int FINS_STATUS_NETWORK_DISABLE = 30;

    public static final String PARAM_DATE = "date";
    public static final String PARAM_ONLY_SET_ALARM = "only_set";
    public static final String PARAM_FROM_ACTIVITY = "from_activity";

    static final String INFO_REFRESH_INTENT = "ru.openitr.cbrfinfo.INFO_UPDATE";
    static final Uri CURRENCYS_URI = Uri.parse("content://ru.openitr.cbrfinfo.currency/currencys");
    private DragSortListView mDslv;
    private DragSortController mController;
    public int dragStartMode = DragSortController.ON_DOWN;
    public boolean removeEnabled = false;
    public int removeMode = DragSortController.FLING_REMOVE;
    public boolean sortEnabled = true;
    public boolean dragEnabled = true;

    CurrencyArrayAdapter ca;

    ArrayList<Icurrency> icurrencies = new ArrayList<Icurrency>();

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    LogSystem.logInFile(LOG_TAG, "Drop from: " + Integer.toString(from) + ", to: " + Integer.toString(to));
                    if (from != to) {
                        moveItem(from, to);
                        //String item = (String) valFromDbAdapter.getItem(from);
                        //adapter.remove(item);
                        //adapter.insert(item, to);
                        //list.moveCheckState(from, to);
                    }
                }
            };


    protected int getLayout() {
        return R.layout.main_currency;
    }

    public static CurrencyInfoFragment newInstance(int headers, int footers) {
        CurrencyInfoFragment f = new CurrencyInfoFragment();

        Bundle args = new Bundle();
        args.putInt("headers", headers);
        args.putInt("footers", footers);
        f.setArguments(args);
        return f;
    }

    public  DragSortController getController(){
        return mController;
    }

    public void setListAdapter(){
        loadCurrencysFromProvider();
        ca = new CurrencyArrayAdapter(getActivity(), icurrencies);
        setListAdapter(ca);
    }

    public DragSortController buildController(DragSortListView dslv){
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.click_remove);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        return controller;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onDate = Calendar.getInstance();
//        context.setContentView(R.layout.main_currency);
        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + ":  onCreate");
//        refreshServiceIntent = new Intent(this, InfoRefreshService.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDslv = (DragSortListView) inflater.inflate(getLayout(), container, false);
        mController = buildController(mDslv);
        mDslv.setFloatViewManager(mController);
        mDslv.setOnTouchListener(mController);
        mDslv.setDragEnabled(dragEnabled);

        return mDslv;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDslv = (DragSortListView) getListView();
        mDslv.setDropListener(onDrop);

        Bundle args = getArguments();
        int headers = 0;
        int footers = 0;

        if (args != null){
            headers = args.getInt("headers");
            footers = args.getInt("footers");
        }

        for (int i = 0; i<headers; i++)
            addHeader(getActivity(), mDslv);
        for (int i = 0; i<footers; i++)
            addFooter(getActivity(), mDslv);

        setListAdapter();

    }

    private void addFooter(FragmentActivity activity, DragSortListView mDslv) {
        LayoutInflater inflater = activity.getLayoutInflater();
        int count = mDslv.getHeaderViewsCount();
        TextView footer  = (TextView) inflater.inflate(R.layout.header_footer, null);
        footer.setText("Footer #" + count + 1);

        mDslv.addHeaderView(footer, null, false);

    }

    private void addHeader(FragmentActivity activity, DragSortListView mDslv) {
        LayoutInflater inflater = activity.getLayoutInflater();
        int count = mDslv.getHeaderViewsCount();
        TextView header  = (TextView) inflater.inflate(R.layout.header_footer, null);
        header.setText("Header #" + count + 1);

        mDslv.addHeaderView(header, null, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intF = new IntentFilter(INFO_REFRESH_INTENT);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogSystem.logInFile(LOG_TAG, this.getClass().getSimpleName() + ": onDestroy");
    }

    /**
     * Загружает курсы валют из базы данных в массив для адаптера.
     */

    private void loadCurrencysFromProvider() {
        icurrencies.clear();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(CURRENCYS_URI, CurrencyDbAdapter.ALL_COLUMNS, null, null, CurrencyDbAdapter.KEY_ORDER + " ASC");
        if (c.getCount() == 0) {
            //getInfo(0);
            c = cr.query(CURRENCYS_URI, CurrencyDbAdapter.ALL_COLUMNS, null, null, CurrencyDbAdapter.KEY_ORDER + " ASC");
        }
        if (c.moveToFirst()) {
            do {
                String vName = c.getString(CurrencyDbAdapter.VALNAME_COLUMN);
                Float vCurs = c.getFloat(CurrencyDbAdapter.VALCURS_COLUMN);
                String vchCode = c.getString(CurrencyDbAdapter.VALCHARCODE_COLUMN);
                int vCode = c.getInt(CurrencyDbAdapter.VALCODE_COLUMN);
                Calendar vDate = Calendar.getInstance();
                vDate.setTimeInMillis(c.getLong(CurrencyDbAdapter.VALDATE_COLUMN));
                Icurrency ic = new Icurrency(vName, vCurs, vchCode, vCode, vDate);
                icurrencies.add(ic);
            } while (c.moveToNext());
        }
        if (ca != null) ca.notifyDataSetChanged();
        c.close();
    }

    private void moveItem(int from, int to) {
        // TODO Сделать сначала перемещние элемента в массиве потом в базе и убрать loadCurrencysFromProvider.
        // TODO Перемещение в базе убрать в отдельный поток.
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues cv = new ContentValues();
        LinkedList<String> items = new LinkedList<String>();
        Cursor itemsCursor = cr.query(CURRENCYS_URI, CurrencyDbAdapter.ALL_COLUMNS, null, null, CurrencyDbAdapter.KEY_ORDER);
        itemsCursor.moveToFirst();
        do {
            items.add(itemsCursor.getString(CurrencyDbAdapter.VALCHARCODE_COLUMN));
        }
        while (itemsCursor.moveToNext());
        String item = items.get(from);
        items.remove(from);
        items.add(to, item);
        for (String itemCode : items) {
            int index = items.indexOf(itemCode);
            cv.put(CurrencyDbAdapter.KEY_ORDER, index);
            cr.update(Uri.parse(CURRENCYS_URI.toString() + "/" + itemCode), cv, null, null);

        }
        itemsCursor.close();
        loadCurrencysFromProvider();
    }


}
