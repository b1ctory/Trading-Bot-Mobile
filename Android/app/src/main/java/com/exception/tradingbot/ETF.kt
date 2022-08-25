package com.exception.tradingbot

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ETF (
    var ticker: String,
    var strategyIncome: Int,
    var buyAndHoldIncome: Int,
    var buyScore: Int,
    var winScore: Int
): Parcelable