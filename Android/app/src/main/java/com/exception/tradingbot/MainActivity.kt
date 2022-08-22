package com.exception.tradingbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.exception.tradingbot.R


//https://ourcodeworld.com/articles/read/1656/how-to-use-chaquopy-to-run-python-code-and-obtain-its-output-using-java-in-your-android-app
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        var py: Python = Python.getInstance()
        var pyObject: PyObject = py.getModule("main")
        var obj: PyObject = pyObject.callAttrThrows("run")

        Log.e("OBJ", "$obj")
    }
}