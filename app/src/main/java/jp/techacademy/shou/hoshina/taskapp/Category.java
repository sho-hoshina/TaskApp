package jp.techacademy.shou.hoshina.taskapp;

/**
 * Created by kuro on 2017/09/16.
 */
import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject implements Serializable {
    private String category;    //カテゴリー

    //idをプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }


    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }
}
