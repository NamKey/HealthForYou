package com.example.nam.healthforyou;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by NAM on 2017-08-21.
 */

public class GridAdapter extends BaseAdapter {
    ArrayList<GridItem> gridItemArrayList = new ArrayList<>();
    LayoutInflater inflater;
    @Override
    public int getCount() {
        return gridItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Context mContext = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.choiceofdata, parent, false);
        }

        Log.d("Grid","GetView");
        GridItem gridItem = gridItemArrayList.get(position);

        TextView grid_userbpm = (TextView)convertView.findViewById(R.id.tvgv_chatHeart1);
        TextView grid_userres = (TextView)convertView.findViewById(R.id.tvgv_chatRes1);
        TextView grid_datasigndate = (TextView)convertView.findViewById(R.id.tvgv_signdate);
        grid_userbpm.setText(gridItem.gv_userbpm+"bpm");
        grid_userres.setText(gridItem.gv_userres+"/min");
        grid_datasigndate.setText("측정날짜 : "+gridItem.gv_signdate);

        return convertView;
    }

    public void addItem(GridItem item)
    {
        gridItemArrayList.add(item);//gridView에 아이템을 추가
    }

    public GridItem getGridItem(int position)
    {
        return gridItemArrayList.get(position);
    }
}
