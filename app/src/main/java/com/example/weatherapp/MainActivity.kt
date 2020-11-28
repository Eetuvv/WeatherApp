package com.example.weatherapp

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.data.WeatherStackApiService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    var cityList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getCitiesFromText()
        var adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            cityList
        )
        listView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onQueryTextSubmit(query: String?): Boolean {
                getWeather(query.toString())
                searchView.setQuery("",false)
                searchView.clearFocus()

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (TextUtils.isEmpty(newText)) { //Define listView visible only when searchView contains text
                    listView.setVisibility(View.GONE)
                } else {
                    listView.setVisibility(View.VISIBLE)
                }
                adapter.filter.filter(newText)
                return true
            }
        })

        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val listItem = listView.getItemAtPosition(position)
                getWeather(listItem.toString())
                listView.setVisibility(View.GONE)
                searchView.setQuery("", false)
                searchView.clearFocus()
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getWeather(location: String) {
        val apiService = WeatherStackApiService()
        GlobalScope.launch(Dispatchers.Main) {

            try {
                val currentWeatherResponse = apiService.getCurrentWeather(location).await() //Get current weather from WeatherAPI
                textViewCity.setText(currentWeatherResponse.location.name)
                temperatureText.text = currentWeatherResponse.current.tempC.toString() + " °C" // Set textfields and icons using JSON-data provided by API
                weatherConditionText.setText(currentWeatherResponse.current.condition.text)
                regionText.setText(currentWeatherResponse.location.region.toString())
                lastUpdateText.setText("Last updated: \n" + currentWeatherResponse.current.lastUpdated.toString())
                Picasso.get()
                    .load("https:" + currentWeatherResponse.current.condition.icon.toString())
                    .into(weatherIcon)
                windText.setText(currentWeatherResponse.current.windKph.toString() + " kp/h")
                humidityText.setText(currentWeatherResponse.current.humidity.toString() + "%")
                windIcon.setVisibility(View.VISIBLE)
                waterIcon.setVisibility(View.VISIBLE)
            } catch (ex: HttpException) { //Catch exception if API returns error message
                Toast.makeText(
                    this@MainActivity,
                    "Could not find location",
                    Toast.LENGTH_SHORT
                ).show()
                searchView.clearFocus()
            }
        }
    }
    fun getCitiesFromText(){ //Read from a text file containing names of major cities
        val inputStream: InputStream = assets.open("citiesList.txt")
        val lineList = mutableListOf<String>()

        inputStream.bufferedReader().forEachLine {lineList.add(it)  }
        for (s in lineList) {
            cityList.add(s)
        }
    }
}
