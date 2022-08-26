package com.exception.tradingbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.layout_dialog.*

class SearchDialogFragment(private val searchViewModel: SearchViewModel, private val isStock: Boolean): DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme_Search)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =  inflater.inflate(R.layout.layout_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tv_dialog_ok.setOnClickListener {
            if (isStock) {
                searchViewModel.setStockSearchAgain(true)
            } else {
                searchViewModel.setETFSearchAgain(true)
            }

            dismiss()
        }

        tv_dialog_cancel.setOnClickListener {
            if (isStock) {
                searchViewModel.setStockSearchAgain(false)
            } else {
                searchViewModel.setETFSearchAgain(false)
            }
            dismiss()
        }

    }

}