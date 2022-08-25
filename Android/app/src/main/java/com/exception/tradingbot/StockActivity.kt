package com.exception.tradingbot

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.exception.tradingbot.databinding.ActivityStockBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.round
import java.time.LocalDate
import kotlin.math.roundToInt

class StockActivity: AppCompatActivity() {
    private lateinit var stockBinding: ActivityStockBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stockBinding = DataBindingUtil.setContentView(this, R.layout.activity_stock)

        Log.e("Shared", SharedPreferenceManager.getYesterDayStock(this).toString())

        stockBinding.indicatorStock.visibility = View.INVISIBLE
        stockBinding.buttonStockSearch.isClickable = true

        val adapter = StockAdapter()
        stockBinding.recyclerviewStock.adapter = adapter
        stockBinding.recyclerviewStock.layoutManager = LinearLayoutManager(this)
        stockBinding.recyclerviewStock.addItemDecoration(StockItemDecoration())

        if (SharedPreferenceManager.getYesterDayStock(this).isNotEmpty()) {
            adapter.resultDataList = SharedPreferenceManager.getYesterDayStock(this)
        }

        stockBinding.buttonStockSearch.setOnClickListener {

            Log.e("NOW", LocalDate.now().toString())

            GlobalScope.launch(Dispatchers.Main) {
                stockBinding.indicatorStock.visibility = View.VISIBLE
                stockBinding.buttonStockSearch.isClickable = false

                if (LocalDate.now().toString() == SharedPreferenceManager.getDate(applicationContext)) {
                    // TODO: Alert for 재검색
                    if (SharedPreferenceManager.getYesterDayStock(applicationContext).isEmpty()) {
                        Log.e("Empty", "Empty")
                        val stockList = searchStock()
                        adapter.resultDataList = stockList
                    } else {
                        Log.e("Not Empty", "Not Empty")
                    }
                } else {
                    SharedPreferenceManager.setDate(applicationContext, LocalDate.now().toString())
                    if (SharedPreferenceManager.getYesterDayStock(applicationContext).isEmpty()) {
                        Log.e("Empty", "Empty")
                        val stockList = searchStock()
                        adapter.resultDataList = stockList
                    } else {
                        Log.e("Not Empty", "Not Empty")
                    }
                }

                delay(1000)

                stockBinding.buttonStockSearch.isClickable = true
                stockBinding.indicatorStock.visibility = View.INVISIBLE

            }

        }

    }



    private fun searchStock(): MutableList<Stock> {
        Log.e("Search Stock", "Start")
        /*
        1. recyclerview 사용해서 종목 / buy score / 전략 수익률 / 바이앤홀드 수익률 / 승률 출력
        2. Thread 사용해서 별도 스레드에서 돌아가면서 indicator view 구현
        */
        var stockList: MutableList<Stock> = mutableListOf()
        try {

            val py: Python = Python.getInstance()
            val main: PyObject = py.getModule("trading_bot")

            // # tickers = ["ADBE", "XLV", "QCOM", "MDLZ", "IAU"]
            val buyStock = main.callAttr("buyStock")
            val buyStockArray = buyStock.asList()

            for (value in buyStockArray) {
                val dic = value.toString().split(",")

                val ticker = dic[0].split(":")[1].trim().replace("'", "")
                val buyScore = dic[1].split(":")[1].trim().toInt()
                val strategyIncome = dic[2].split(":")[1].trim().toFloat().roundToInt()
                val buyAndHoldIncome = dic[3].split(":")[1].trim().toFloat().roundToInt()
                val winScore = dic[4].split(":")[1].trim().replace("}", "").substring(0, 3).toFloat().times(100).toInt()

                // TODO : SharedPreferences에 저장해서 전날 기록 볼 수 있도록


                val stock = Stock(ticker, strategyIncome, buyAndHoldIncome,  buyScore, winScore)
                stockList.add(stock)

            }
        } catch(e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("Python", e.message.toString())
        }

        SharedPreferenceManager.setYesterDayStock(this, stockList)

        return stockList
    }
}