package jp.techacademy.shou.hoshina.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputCategory extends AppCompatActivity {
    private EditText mCategoryEdit;
    private Category mCategory;

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addCategory();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_category);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mCategoryEdit = (EditText)findViewById(R.id.category_edit_text);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();
        int categoryID = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        mCategory = realm.where(Category.class).equalTo("id", categoryID).findFirst();
        realm.close();

        if(mCategory == null){
            //新規作成の場合
        }else{
            //更新の場合
            mCategoryEdit.setText(mCategory.getCategory());
        }
    }

    private void addCategory(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        if (mCategory == null) {
            // 新規作成の場合
            mCategory = new Category();

            RealmResults<Category> categoryRealmResults = realm.where(Category.class).findAll();

            int identifier;
            if (categoryRealmResults.max("id") != null) {
                identifier = categoryRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mCategory.setId(identifier);
        }

        String category = mCategoryEdit.getText().toString();

        mCategory.setCategory(category);

        realm.copyToRealmOrUpdate(mCategory);
        realm.commitTransaction();

        realm.close();
    }


}
