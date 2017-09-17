package jp.techacademy.shou.hoshina.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.taro.kirameki.taskapp.TASK";
    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadCategorySpinner();
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private Button mSearchButton;
    private Spinner mCategorySpinner;

    // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
    private AdapterView.OnItemSelectedListener mOnCategoryItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner) parent;
            // 選択されたアイテムを取得します
            String item = (String) spinner.getSelectedItem();
            Log.d("ANDROID", item);
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        mSearchButton = (Button)findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reloadListView();
            }
        });
        mCategorySpinner = (Spinner)findViewById(R.id.category_spinner);
        mCategorySpinner.setOnItemSelectedListener(mOnCategoryItemSelectedListener);
        reloadCategorySpinner();


        //Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        //ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView)findViewById(R.id.listView1);

        //ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        //ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                //タスクを削除する
                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
    }

    private void reloadListView(){

        //絞込み条件を設定(カテゴリーで絞り込む)
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("*");
        sbuf.append((String)mCategorySpinner.getSelectedItem());
        sbuf.append("*");
        String text = sbuf.toString();

        //Realmデータベースから、「カテゴリーが絞込み条件に一致したもの」を取得して新しい日時順に並べた結果を取得
        //*を使用しているので未記入の場合、すべての項目が表示される。
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class)
                                                .like("category", text)
                                                .findAllSorted("date", Sort.DESCENDING);

        //上記の結果を、TaskListとしてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        //TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        //表示を更新するために、アダプタにデータが変更されたことを知らせる。
        mTaskAdapter.notifyDataSetChanged();
    }

    private void reloadCategorySpinner(){
        Realm realm = Realm.getDefaultInstance();

        //Realmデータベースから、「カテゴリー」を取得して名前順に並べた結果を取得
        RealmResults<Category> categoryRealmResults = realm.where(Category.class)
                .findAllSorted("category", Sort.ASCENDING);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        //adapter.add("カテゴリー");   //新規作成時のコメント用
        for(Category category : categoryRealmResults){
            adapter.add(category.getCategory());
            Log.d("ANDROID", String.format("ID: %s  Category: %s", category.getId(), category.getCategory()));
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        realm.close();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mRealm.close();
    }

}
