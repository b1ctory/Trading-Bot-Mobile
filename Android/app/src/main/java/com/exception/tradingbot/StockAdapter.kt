package com.exception.tradingbot

import android.content.res.Resources
import android.provider.Settings.Secure.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockAdapter: RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    var resultDataList = mutableListOf<Stock>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockAdapter.StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockAdapter.StockViewHolder, position: Int) {
        val data = resultDataList.get(position)
        holder.setDataText(data)
    }

    override fun getItemCount(): Int {
        return resultDataList.count()
    }

    class StockViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tickerName: TextView = itemView.findViewById<TextView>(R.id.textview_ticker_name_result)
        var buyScore: TextView = itemView.findViewById<TextView>(R.id.textview_buyscore)
        var winScore: TextView = itemView.findViewById<TextView>(R.id.textview_win_score)
        var strategyReturnValue: TextView = itemView.findViewById<TextView>(R.id.textview_strategy_return_result)
        var buyAndHoldReturnValue: TextView = itemView.findViewById<TextView>(R.id.textview_buy_and_hold_income_result)

        fun setDataText(stockData: Stock) {
            tickerName.text = stockData.ticker
            buyScore.text = itemView.resources.getString(R.string.buy_score, stockData.buyScore.toString())
            winScore.text = itemView.resources.getString(R.string.win_score, stockData.winScore.toString())
            strategyReturnValue.text = itemView.resources.getString(R.string.strategy_income, stockData.strategyIncome.toString())
            buyAndHoldReturnValue.text =  itemView.resources.getString(R.string.buy_and_hold_income, stockData.buyAndHoldIncome.toString())
        }
    }
}