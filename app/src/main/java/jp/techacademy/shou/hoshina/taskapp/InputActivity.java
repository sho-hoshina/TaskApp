package jp.techacademy.shou.hoshina.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.RealmChangeListener;
import io.realm.Sort;

import android.util.Log;

public class InputActivity extends AppCompatActivity {
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mDateButton, mTimeButton;
    private EditText mTitleEdit, mContentEdit;
    private Spinner mCategorySpinner;
    private Task mTask;
    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadCategorySpinner();
        }
    };

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };

    private View.OnClickListener mOnCreateCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InputActivity.this, InputCategory.class);

            startActivity(intent);
        }
    };

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
        setContentView(R.layout.activity_input);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        findViewById(R.id.createCategory_button).setOnClickListener(mOnCreateCategoryClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategorySpinner = (Spinner)findViewById(R.id.category_spinner);
        mCategorySpinner.setOnItemSelectedListener(mOnCategoryItemSelectedListener);
        reloadCategorySpinner();

        //Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();
        realm.close();

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());
            setSpinnerSelection(mCategorySpinner, mTask.getCategory());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    }

    private void reloadCategorySpinner(){
        Realm realm = Realm.getDefaultInstance();

        //Realmデータベースから、「カテゴリー」を取得して名前順に並べた結果を取得
        RealmResults<Category> categoryRealmResults = realm.where(Category.class)
                .findAllSorted("category", Sort.ASCENDING);

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        //adapter.add("カテゴリー");   //新規作成時のコメント用
        for(Category category : categoryRealmResults){
            adapter.add(category.getCategory());
            Log.d("ANDROID", String.format("ID: %s  Category: %s", category.getId(), category.getCategory()));
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        realm.close();
    }

    private void setSpinnerSelection(Spinner spinner, String item){
        SpinnerAdapter adapter = spinner.getAdapter();
        int idx = 0;
        for(int i = 0; i < adapter.getCount(); i++){
            if(adapter.getItem(i).equals(item)) {
                idx = i;
                break;
            }
        }
        spinner.setSelection(idx);
    }

    private void addTask(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();
        String category = (String)mCategorySpinner.getSelectedItem();

        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(category);
        GregorianCalendar calendar = new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mRealm.close();
    }
}
