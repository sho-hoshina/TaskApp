package jp.techacademy.shou.hoshina.taskapp;

/**
 * Created by kuro on 2017/09/08.
 */

import android.app.Application;
import io.realm.Realm;

public class TaskApp extends Application{
    @Override
    public void onCreate(){
        super.onCreate();
        Realm.init(this);
    }
}
