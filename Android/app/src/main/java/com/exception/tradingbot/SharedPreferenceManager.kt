package com.exception.tradingbot

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

public object SharedPreferenceManager {
    fun setYesterDayStock(context: Context, list:MutableList<Stock>) {
        val prefs : SharedPreferences = context.getSharedPreferences("yesterdayStock", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        var string = "["
        for (i in 0 until list.size) {
            string += Gson().toJson(list[i])
            if(list[i]!=list.last()) {
                string += ","
            }
        }
        string+="]"
        editor.putString("YESTERDAY_STOCK", string)
        editor.apply()
    }

    fun getYesterDayStock(context: Context) : MutableList<Stock> {
        val prefs : SharedPreferences = context.getSharedPreferences("yesterdayStock", Context.MODE_PRIVATE)
        val list = arrayListOf<Stock>()
        val array :Array<Stock>? = Gson().fromJson(prefs.getString("YESTERDAY_STOCK", ""),
            Array<Stock>::class.java)
        if(array != null) {
            for (i in array) {
                list.add(i)
            }
        }
        return list
    }

    fun setYesterDayETF(context: Context, list:MutableList<ETF>) {
        val prefs : SharedPreferences = context.getSharedPreferences("yesterdayETF", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        var string = "["
        for (i in 0 until list.size) {
            string += Gson().toJson(list[i])
            if(list[i]!=list.last()) {
                string += ","
            }
        }
        string+="]"
        editor.putString("YESTERDAY_ETF", string)
        editor.apply()
    }

    fun getYesterDayETF(context: Context) : MutableList<ETF> {
        val prefs : SharedPreferences = context.getSharedPreferences("yesterdayETF", Context.MODE_PRIVATE)
        val list = arrayListOf<ETF>()
        val array :Array<ETF>? = Gson().fromJson(prefs.getString("YESTERDAY_ETF", ""),
            Array<ETF>::class.java)
        if(array != null) {
            for (i in array) {
                list.add(i)
            }
        }
        return list
    }

    fun setDate(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences("date", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("SEARCH_DATE", input)
        editor.commit()
    }

    fun getDate(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences("date", Context.MODE_PRIVATE)
        return prefs.getString("SEARCH_DATE", "").toString()
    }
}