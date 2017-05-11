package com.imooc.cxj.wuziqi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.imooc.cxj.wuziqi.ChessView;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_reatart;
    private ChessView chessView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_reatart = (Button) findViewById(R.id.bt_restart);
        chessView= (ChessView) findViewById(R.id.custon_chess_main);
        btn_reatart.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_restart:
                chessView.myreStart();
                break;
        }
    }
}