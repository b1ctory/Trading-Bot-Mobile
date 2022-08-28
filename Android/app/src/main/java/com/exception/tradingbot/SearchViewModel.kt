package com.exception.tradingbot

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel(): ViewModel() {

    val isStockProgressBarStart = MutableLiveData<Boolean>()
    fun setStockProgressBarStart(value: Boolean) {
        isStockProgressBarStart.postValue(value)
    }

    val isStockSearchAgain = MutableLiveData<Boolean>()
    fun setStockSearchAgain(value: Boolean) {
        isStockSearchAgain.postValue(value)
    }

    val isETFProgressBarStart = MutableLiveData<Boolean>()
    fun setETFProgressBarStart(value: Boolean) {
        isETFProgressBarStart.postValue(value)
    }

    val isETFSearchAgain = MutableLiveData<Boolean>()
    fun setETFSearchAgain(value: Boolean) {
        isETFProgressBarStart.postValue(value)
    }
}