package com.example.nam.healthforyou;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONObject;

import java.util.ArrayList;

public class Choosedata extends AppCompatActivity {
    DBhelper dBhelper;
    ArrayList<JSONObject> gridJSONarray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosedata);
        dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
        GridView dataGrid = (GridView)findViewById(R.id.gv_healthdata);//GridView 찾기
        final GridAdapter gridAdapter = new GridAdapter();//GridView Adapter
        dataGrid.setAdapter(gridAdapter);

        gridJSONarray=dBhelper.PrintMyAvgDataForgridView("SELECT avg(user_bpm),avg(user_res),strftime('%Y-%m-%d',data_signdate) as date from User_health GROUP BY strftime('%Y-%m-%d',data_signdate) ORDER BY date desc;");
        System.out.println(gridJSONarray);

        for(int i=0;i<gridJSONarray.size();i++)
        {
            JSONObject jsonObject = gridJSONarray.get(i);
            ////JSON -> gridItem
            GridItem gridItem = new GridItem();
            gridItem.gv_userbpm = jsonObject.optInt("user_bpm");
            gridItem.gv_userres = jsonObject.optInt("user_res");
            gridItem.gv_signdate = jsonObject.optString("data_signdate");

            gridAdapter.addItem(gridItem);
        }
        gridAdapter.notifyDataSetChanged();

        dataGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                GridItem gridItem = gridAdapter.getGridItem(position);
                intent.putExtra("date",gridItem.gv_signdate);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
}
