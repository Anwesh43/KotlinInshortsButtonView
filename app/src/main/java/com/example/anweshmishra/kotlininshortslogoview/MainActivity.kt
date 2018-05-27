package com.example.anweshmishra.kotlininshortslogoview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.inshortslogoview.InshortsLogoView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InshortsLogoView.create(this)
    }
}
