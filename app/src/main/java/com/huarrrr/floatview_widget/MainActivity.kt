package com.huarrrr.floatview_widget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.huarrrr.floatview.FloatingViewKotlin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FloatingViewKotlin(this, onClickAction = {
        }).apply {
            showFloat()
        }

//        FloatingViewJava floatView = FloatingViewJava(this) {
//            //点击
//        }
//        floatView.showFloat();
    }
}