package com.exception.tradingbot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.exception.tradingbot.R
import com.exception.tradingbot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }


        mainBinding.btnStock.setOnClickListener {
            onClickStockButton()
        }


        mainBinding.btnEtf.setOnClickListener {
            onClickETFButton()

        }

        mainBinding.btnBuyOne.setOnClickListener {

        }

    }

    private fun onClickETFButton() {
        val intent = Intent(this, ETFActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.animation_in, R.anim.animation_out)
    }

    private fun onClickStockButton() {
        val intent = Intent(this, StockActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.animation_in, R.anim.animation_out)
    }

}