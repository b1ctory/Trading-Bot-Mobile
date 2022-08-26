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
import com.exception.tradingbot.databinding.ActivityEtfBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class ETFActivity: AppCompatActivity() {

    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var etfBinding: ActivityEtfBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        etfBinding = DataBindingUtil.setContentView(this, R.layout.activity_etf)

        etfBinding.indicatorETF.visibility = View.INVISIBLE
        etfBinding.buttonEtfSearch.isClickable = true


        val adapter = ETFAdapter()
        etfBinding.recyclerviewEtf.adapter = adapter
        etfBinding.recyclerviewEtf.layoutManager = LinearLayoutManager(this)
        etfBinding.recyclerviewEtf.addItemDecoration(ETFItemDecoration())

        if (SharedPreferenceManager.getYesterDayETF(this).isNotEmpty()) {
            adapter.resultDataList = SharedPreferenceManager.getYesterDayETF(this)
        }

        etfBinding.buttonEtfSearch.setOnClickListener {
            searchViewModel.setETFProgressBarStart(true)
            GlobalScope.launch(Dispatchers.Main) {
                etfBinding.indicatorETF.visibility = View.VISIBLE
                etfBinding.buttonEtfSearch.isClickable = false

                if (SharedPreferenceManager.getYesterDayETF(applicationContext).isEmpty()) {
                    Log.e("GetYesterDayETF", "Empty")
                    SharedPreferenceManager.setETFDate(applicationContext, LocalDate.now().toString())
                    val etfList = searchETF()
                    etfBinding.recyclerviewEtf.adapter = adapter
                    etfBinding.recyclerviewEtf.layoutManager = LinearLayoutManager(applicationContext)
                    etfBinding.recyclerviewEtf.addItemDecoration(ETFItemDecoration())
                    adapter.resultDataList = etfList
                } else {
                    if (LocalDate.now().toString() == SharedPreferenceManager.getETFDate(applicationContext)) {
                        val dialog = SearchDialogFragment(searchViewModel, isStock = false)
                        dialog.show(supportFragmentManager, dialog.tag)

                        if (searchViewModel.isETFSearchAgain.value == true) {
                            searchViewModel.setETFProgressBarStart(true)
                            val etfList = searchETF()
                            etfBinding.recyclerviewEtf.adapter = adapter
                            etfBinding.recyclerviewEtf.layoutManager = LinearLayoutManager(applicationContext)
                            etfBinding.recyclerviewEtf.addItemDecoration(ETFItemDecoration())
                            adapter.resultDataList = etfList
                        }
                    } else {
                        SharedPreferenceManager.setETFDate(applicationContext, LocalDate.now().toString())
                        val etfList = searchETF()
                        etfBinding.recyclerviewEtf.adapter = adapter
                        etfBinding.recyclerviewEtf.layoutManager = LinearLayoutManager(applicationContext)
                        etfBinding.recyclerviewEtf.addItemDecoration(ETFItemDecoration())
                        adapter.resultDataList = etfList
                    }
                }
                searchViewModel.setETFProgressBarStart(false)
            }
        }
        searchViewModel.isETFProgressBarStart.observeForever {
            if (it) {
                etfBinding.indicatorETF.visibility = View.VISIBLE
                etfBinding.etfTouchArea.isClickable = false
                etfBinding.buttonEtfSearch.isClickable = false
            } else {
                etfBinding.indicatorETF.visibility = View.INVISIBLE
                etfBinding.etfTouchArea.isClickable = true
                etfBinding.buttonEtfSearch.isClickable = true
            }
        }
    }

    private fun searchETF(): MutableList<ETF> {
        Log.e("Search ETF", "Start")

        var etfList: MutableList<ETF> = mutableListOf()
        try {

            val py: Python = Python.getInstance()
            val main: PyObject = py.getModule("trading_bot")

            // # tickers = ["ADBE", "XLV", "QCOM", "MDLZ", "IAU"]
            val buyETF = main.callAttr("buyETF")
            val buyETFArray = buyETF.asList()

            for (value in buyETFArray) {
                val dic = value.toString().split(",")

                val ticker = dic[0].split(":")[1].trim().replace("'", "")
                val buyScore = dic[1].split(":")[1].trim().toInt()
                val strategyIncome = dic[2].split(":")[1].trim().toFloat().roundToInt()
                val buyAndHoldIncome = dic[3].split(":")[1].trim().toFloat().roundToInt()
                val winScore = dic[4].split(":")[1].trim().replace("}", "").substring(0, 3).toFloat().times(100).toInt()

                // TODO : SharedPreferences에 저장해서 전날 기록 볼 수 있도록


                val etf = ETF(ticker, strategyIncome, buyAndHoldIncome,  buyScore, winScore)
                etfList.add(etf)

            }
        } catch(e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("Python", e.message.toString())
        }

        SharedPreferenceManager.setYesterDayETF(this, etfList)

        return etfList
    }

}