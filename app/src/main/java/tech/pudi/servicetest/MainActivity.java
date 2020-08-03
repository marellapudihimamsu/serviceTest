package tech.pudi.servicetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button start,stop;
    SQLiteDatabase localphonedb;
    String ans;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start=findViewById(R.id.start);
        stop=findViewById(R.id.stop);
        textView=findViewById(R.id.textview);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(MainActivity.this,MyService.class));
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this,MyService.class));
            }
        });
        localphonedb=openOrCreateDatabase("phone.db", Context.MODE_PRIVATE,null);
        localphonedb.execSQL("CREATE TABLE IF NOT EXISTS cont(name varchar,number int)");
        Cursor resultset = localphonedb.rawQuery("Select * from cont",null);
        if(resultset.getCount()>0){
            resultset.moveToNext();
            ans="";
            do {
                ans+=resultset.getString(0)+" : "+resultset.getInt(1)+"\n";
                Log.i("name ",resultset.getString(0));
            }
            while (resultset.moveToNext());

        }
        textView.setText(ans);
    }
}
