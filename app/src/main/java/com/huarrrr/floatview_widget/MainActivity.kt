package com.huarrrr.floatview_widget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FloatingViewKotlin(this, onClickAction = {
            Toast.makeText(this, "click", Toast.LENGTH_SHORT).show()
        }).apply {
            showFloat()
        }


//        FloatingViewJava floatView = FloatingViewJava(this) {
//            //点击
//        }
//        floatView.showFloat();
    }
}