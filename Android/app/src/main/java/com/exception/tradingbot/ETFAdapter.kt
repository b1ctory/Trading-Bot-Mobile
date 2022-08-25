package com.exception.tradingbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ETFAdapter: RecyclerView.Adapter<ETFAdapter.ETFViewHolder>() {

    // TODO: Need Update to Data Class
    var resultDataList = mutableListOf<ETF>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ETFAdapter.ETFViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ETFViewHolder(view)
    }

    override fun onBindViewHolder(holder: ETFAdapter.ETFViewHolder, position: Int) {
        val data = resultDataList[position]
        holder.setDataText(data)
    }

    override fun getItemCount(): Int {
        return resultDataList.count()
    }

    class ETFViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tickerName: TextView = itemView.findViewById<TextView>(R.id.textview_ticker_name_result)
        var buyScore: TextView = itemView.findViewById<TextView>(R.id.textview_buyscore)
        var winScore: TextView = itemView.findViewById<TextView>(R.id.textview_win_score)
        var strategyReturnValue: TextView = itemView.findViewById<TextView>(R.id.textview_strategy_return_result)
        var buyAndHoldReturnValue: TextView = itemView.findViewById<TextView>(R.id.textview_buy_and_hold_income_result)

        fun setDataText(etfData: ETF) {
            tickerName.text = etfData.ticker
            buyScore.text = itemView.resources.getString(R.string.buy_score, etfData.buyScore.toString())
            winScore.text = itemView.resources.getString(R.string.win_score, etfData.winScore.toString())
            strategyReturnValue.text = itemView.resources.getString(R.string.strategy_income, etfData.strategyIncome.toString())
            buyAndHoldReturnValue.text =  itemView.resources.getString(R.string.buy_and_hold_income, etfData.buyAndHoldIncome.toString())
        }
    }
}