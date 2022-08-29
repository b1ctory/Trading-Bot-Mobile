package com.exception.tradingbot

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ETF (
    var name: String,
    var strategy_yield: Int,
    var buy_and_hold_yield: Int,
    var buy_score: Int,
    var win_rate: Int
): Parcelable