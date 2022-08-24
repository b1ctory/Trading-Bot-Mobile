package com.exception.tradingbot

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.exception.tradingbot.databinding.ActivityEtfBinding

class ETFActivity: AppCompatActivity() {

    private lateinit var etfBinding: ActivityEtfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        etfBinding = DataBindingUtil.setContentView(this, R.layout.activity_etf)

        etfBinding.buttonEtfSearch.setOnClickListener {
            searchETF()
        }

    }

    private fun searchETF() {
        Log.e("Search ETF", "Start")
        /*
        1. recyclerview 사용해서 종목 / buy score / 전략 수익률 / 바이앤홀드 수익률 / 승률 출력
        2. Thread 사용해서 별도 스레드에서 돌아가면서 indicator view 구현
        */
        try {

            val py: Python = Python.getInstance()
            val main: PyObject = py.getModule("trading_bot")

            // # tickers = ["ADBE", "XLV", "QCOM", "MDLZ", "IAU"]
            var buyETF = main.callAttr("buyETF")
            var buyETFToArray = buyETF.toJava(MutableList::class.java)
            Log.e("Buy ETF", buyETFToArray[0].toString())

            for (value in buyETFToArray) {
                Log.e("Buy ETF", value.toString())
            }

        } catch(e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("Python", e.message.toString())
        }
    }

}