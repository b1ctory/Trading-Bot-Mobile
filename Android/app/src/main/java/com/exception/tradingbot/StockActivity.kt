package com.exception.tradingbot

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.exception.tradingbot.databinding.ActivityStockBinding
import kotlinx.coroutines.*
import java.time.LocalDate
import kotlin.math.roundToInt

class StockActivity: AppCompatActivity() {
    private lateinit var stockBinding: ActivityStockBinding
    private val searchViewModel: SearchViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stockBinding = DataBindingUtil.setContentView(this, R.layout.activity_stock)

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
            searchViewModel.setStockProgressBarStart(true)
            Log.e("현재 날짜", LocalDate.now().toString())

            GlobalScope.launch(Dispatchers.Main) {

                stockBinding.indicatorStock.visibility = View.VISIBLE
                stockBinding.buttonStockSearch.isClickable = false
                // 앱에 최초 진입한 경우
                if (SharedPreferenceManager.getYesterDayStock(applicationContext).isEmpty()) {
                    Log.e("GetYesterDayStock", "Empty")
                    SharedPreferenceManager.setStockDate(applicationContext, LocalDate.now().toString())
                    val stockList = searchStock()
                    stockBinding.recyclerviewStock.adapter = adapter
                    stockBinding.recyclerviewStock.layoutManager = LinearLayoutManager(applicationContext)
                    stockBinding.recyclerviewStock.addItemDecoration(StockItemDecoration())
                    adapter.resultDataList = stockList
                } else {
                    // 현재 날짜와 저장된 날짜가 같으면 팝업 로드
                    Log.e("Now", LocalDate.now().toString())
                    Log.e("Saved", SharedPreferenceManager.getStockDate(applicationContext))
                    if (LocalDate.now().toString() == SharedPreferenceManager.getStockDate(applicationContext)) {
                        val dialog = SearchDialogFragment(searchViewModel, isStock = true)
                        dialog.show(supportFragmentManager, dialog.tag)

                        // viewmodel observing해서 stockList search
                        if (searchViewModel.isStockSearchAgain.value == true) {
                            searchViewModel.setStockProgressBarStart(true)
                            val stockList = searchStock()
                            stockBinding.recyclerviewStock.adapter = adapter
                            stockBinding.recyclerviewStock.layoutManager = LinearLayoutManager(applicationContext)
                            stockBinding.recyclerviewStock.addItemDecoration(StockItemDecoration())
                            adapter.resultDataList = stockList
                        }
                    } else {
                        SharedPreferenceManager.setStockDate(applicationContext, LocalDate.now().toString())
                        val stockList = searchStock()
                        stockBinding.recyclerviewStock.adapter = adapter
                        stockBinding.recyclerviewStock.layoutManager = LinearLayoutManager(applicationContext)
                        stockBinding.recyclerviewStock.addItemDecoration(StockItemDecoration())
                        adapter.resultDataList = stockList
                    }

                }
                searchViewModel.setStockProgressBarStart(false)
            }
        }
        searchViewModel.isStockProgressBarStart.observeForever {
            if (it) {
                stockBinding.indicatorStock.visibility = View.VISIBLE
                stockBinding.stockTouchArea.isClickable = false
                stockBinding.buttonStockSearch.isClickable = false
            } else {
                stockBinding.indicatorStock.visibility = View.INVISIBLE
                stockBinding.stockTouchArea.isClickable = true
                stockBinding.buttonStockSearch.isClickable = true
            }
        }
    }



    private fun searchStock(): MutableList<Stock> {
        Log.e("Search Stock", "Start")

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