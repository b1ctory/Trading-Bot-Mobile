package com.exception.tradingbot

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Stock (
    var ticker: String,
    var buyScore: Int,
    var strategyIncome: Double,
    var buyAndHoldIncome: Double,
    var winScore: Double
): Parcelable