package com.example.ictproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG="@@@MainActivity";
    ListView listView;
    listAdapter adapter;
    List<Data> list;
    private EditText editSearch;
    private SearchAdapter s_adapter;
    private ArrayList<Data> arrayList;
    private long pressedTime = 0;
    public static Context mContext;
//    SQLiteDatabase recipeDB = null;

    TextView tv_input_length;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // 조미료통을 설정하지 않았으면 설정화면으로 이동
        String[] condiment = {PreferenceManager.getString(this, "condiment0"), PreferenceManager.getString(this, "condiment1")};
        if(condiment[0].equals("") || condiment[1].equals("")){
            Log.d(TAG, "onCreate: InitialActivity 이동");
            Intent intent = new Intent(MainActivity.this, InitialActivity.class);
            startActivity(intent);
        }

//        setDB();
        updateLV();
        setListner();

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        ImageButton quesbtn = findViewById(R.id.main_ques);
        quesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                startActivity(intent);
            }
        });


        Button bt_rm = findViewById(R.id.bt_remove);
        bt_rm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.clear(mContext);
                Intent intent = new Intent(MainActivity.this, ChangeSpiceActivity.class);
                startActivity(intent);
            }
        });

        Button bt_play = findViewById(R.id.bt_play);
        bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        //search
        editSearch = (EditText) findViewById(R.id.search);
        tv_input_length = findViewById(R.id.tv_input_length);
        arrayList = new ArrayList<>();
        arrayList.addAll(list);

        s_adapter = new SearchAdapter(list, this);
        listView.setAdapter(adapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                tv_input_length.setText(editable.length()+"글자");
                String text = editSearch.getText().toString();
                search(text);
            }
        });

        //EditText Enter key 방지
        /*editSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==event.KEYCODE_ENTER) return true;
                return false;
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        updateLV();
    }
//
//    public void setDB(){
//        try {
//            recipeDB = this.openOrCreateDatabase(DataBase.RecipeEntry.DB_NAME, MODE_PRIVATE, null);
//
//            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + DataBase.RecipeEntry.TABLE_NAME + "( "
//                    + DataBase.RecipeEntry.COLUMN_NAME_ID + " integer primary key autoincrement, "
//                    + DataBase.RecipeEntry.COLUMN_NAME_TITLE + " text, "
//                    + DataBase.RecipeEntry.COLUMN_NAME_CONDIMENTS0 + " text, "
//                    + DataBase.RecipeEntry.COLUMN_NAME_CONDIMENTS1 + " text, "
//                    + DataBase.RecipeEntry.COLUMN_NAME_GRAM0 + " integer, "
//                    + DataBase.RecipeEntry.COLUMN_NAME_GRAM1 + " integer"
//                    + ");";
//
//            recipeDB.execSQL(createTableSQL);
//            recipeDB.close();
//        }
//        catch(Exception e){
//            e.printStackTrace();
//            Log.d(TAG, "setDB: exception");
//        }
//    }

    public void updateLV() {
        Log.d(TAG, "updateLV: ");

        list = new ArrayList<>();
        listView = findViewById(R.id.list_main);
        try {
//            recipeDB = this.openOrCreateDatabase(DataBase.RecipeEntry.DB_NAME, MODE_PRIVATE, null);

            String[] condiment = {PreferenceManager.getString(this, "condiment0"), PreferenceManager.getString(this, "condiment1")};
            String myJSON = new GetData().execute("getData.php", "recipe", "all").get();
            Log.d(TAG, "onCreate: " + myJSON);

            adapter = new listAdapter(list);
            listView.setAdapter(adapter);


            // 레시피 리스트 받아오기
//            String selectSQL = "SELECT * FROM "+DataBase.RecipeEntry.TABLE_NAME;
//            Cursor c = recipeDB.rawQuery(selectSQL, null);

            if(myJSON.equals("")){
                Log.d(TAG, "updateLV: DB is null");
                findViewById(R.id.tv_navigation).setVisibility(View.VISIBLE);
                list.clear();
            }
            else {
                Log.d(TAG, "updateLV: DB is not null");
                findViewById(R.id.tv_navigation).setVisibility(View.INVISIBLE);
//                while (c.moveToNext()) {
//                    int id = c.getInt(c.getColumnIndex(DataBase.RecipeEntry.COLUMN_NAME_ID));
//                    String title = c.getString(c.getColumnIndex(DataBase.RecipeEntry.COLUMN_NAME_TITLE));
//                    String condiment0 = c.getString(c.getColumnIndex(DataBase.RecipeEntry.COLUMN_NAME_CONDIMENTS0));
//                    String condiment1 = c.getString(c.getColumnIndex(DataBase.RecipeEntry.COLUMN_NAME_CONDIMENTS1));
//                    int gram0 = c.getInt(c.getColumnIndex(DataBase.RecipeEntry.COLUMN_NAME_GRAM0));
//                    int gram1 = c.getInt(c.getColumnIndex(DataBase.RecipeEntry.COLUMN_NAME_GRAM1));
//
//                    Log.d(TAG, "updateLV: for loop number : " + c.getCount());
//                    Data data = new Data();
//
//                    data.setId(id);
//                    data.setTitle(title);
//                    data.setCondiment0(condiment0);
//                    data.setCondiment1(condiment1);
//                    data.setGram0(gram0);
//                    data.setGram1(gram1);
//
//                    list.add(data);
//                }
//                c.close();
                JSONObject jsonObject = new JSONObject(myJSON);
                JSONArray dataJSON = jsonObject.getJSONArray(getString(R.string.DB_tableName));

                int num = dataJSON.length();
                for (int i = 0; i < num; i++) {
                    Log.d(TAG, "updateLV: for loop number : " + i);
                    JSONObject c = dataJSON.getJSONObject(i);
                    int id = c.getInt(getString(R.string.DB_colID));
                    int[] gram = {Integer.parseInt(c.getString(getString(R.string.DB_colCondiment0))), Integer.parseInt(c.getString(getString(R.string.DB_colCondiment1)))};
                    Data data = new Data();

                    data.setId(id);
                    data.setTitle(c.getString(getString(R.string.DB_colTitle)));
                    data.setCondiment0(condiment[0]);
                    data.setCondiment1(condiment[1]);
                    data.setGram0(gram[0]);
                    data.setGram1(gram[1]);

                    list.add(data);
                }
            }

            adapter = new listAdapter(list);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

//            recipeDB.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListner(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("실행 하시겠습니까?");
                builder.setTitle("알림창")
                        .setCancelable(false)
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // 레시피 실행
                                String gram0 = Integer.toString(list.get(position).getGram0());
                                String gram1 = Integer.toString(list.get(position).getGram1());

                                InsertData task = new InsertData();
                                task.execute("executeData.php",
                                        getString(R.string.DB_colCondiment0), gram0,
                                        getString(R.string.DB_colCondiment1), gram1
                                );
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("알림창");
                alert.show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle("알림창").setMessage("원하시는 기능을 선택해주세요.");

                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "onClick: 삭제 요청");

                        InsertData task = new InsertData();
                        task.execute("delData.php", getString(R.string.DB_colID), Integer.toString(list.get(position).getId()));
//                        try{
//                            recipeDB = openOrCreateDatabase(DataBase.RecipeEntry.DB_NAME, MODE_PRIVATE, null);
//
//                            String deleteRecordSQL = "DELETE FROM "+ DataBase.RecipeEntry.TABLE_NAME
//                                    + "WHERE id = "+id+";";
//                            recipeDB.execSQL(deleteRecordSQL);
//
//                        } catch (Exception e){
//                            e.printStackTrace();
//                        }
                    }
                });

                builder.setNegativeButton("수정", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "onClick: 수정 요청");
                        Intent intent = new Intent(MainActivity.this, AddActivity.class);
                        intent.putExtra("id", list.get(position).getId());
                        intent.putExtra("position", position);
                        intent.putExtra("title", list.get(position).getTitle());
                        intent.putExtra("condiment0", list.get(position).getCondiment0());
                        intent.putExtra("condiment1", list.get(position).getCondiment1());
                        intent.putExtra("gram0", Integer.toString(list.get(position).getGram0()));
                        intent.putExtra("gram1", Integer.toString(list.get(position).getGram1()));

                        startActivity(intent);
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if ( pressedTime == 0 ) {
            Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_LONG).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if ( seconds > 1000 ) {
                Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_LONG).show();
                pressedTime = 0 ;
            }
            else {
                super.onBackPressed();
                finish(); // app 종료 시키기
            }
        }
    }

    public void search(String charText) {
        list.clear();

        if(charText.length() == 0) {
            list.addAll(arrayList);
        }
        else {
            for(int i = 0; i < arrayList.size(); i++){
                if(arrayList.get(i).getTitle().contains(charText)){
                    list.add(arrayList.get(i));
                }
            }
        }
        s_adapter.notifyDataSetChanged();
    }


}
