package jp.techacademy.shou.hoshina.taskapp;

/**
 * Created by kuro on 2017/09/08.
 */
import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable{
    private String title;       //タイトル
    private String contents;   //内容
    private Date date;          //日時
    private String category;   //カテゴリー

    //idをプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContents(){
        return contents;
    }

    public void setContents(String contents){
        this.contents = contents;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }

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
