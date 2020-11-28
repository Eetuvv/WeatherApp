package com.example.weatherapp.data
import java.net.URL
import android.util.Log


class Request(private val url: String) {

    fun run() {
        val forecastJson = URL(url).readText()
        Log.d(javaClass.simpleName, forecastJson)
    }
}