package com.example.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.api.model.WeatherObj
import com.google.gson.JsonArray
import com.google.gson.JsonObject

import com.koushikdutta.async.future.FutureCallback

import com.koushikdutta.ion.Ion
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val API_KEY: String = "fc04615f6c90ae64cba31f1748c56873"

    private lateinit var edtCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var imgStatus: ImageView
    private lateinit var tvTemp: TextView
    private lateinit var tvCountry: TextView
    private lateinit var tvDes: TextView
    private lateinit var lv: ListView

    private lateinit var mList: ArrayList<WeatherObj>
    private lateinit var myAdapter: MyArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapping()
        mList = arrayListOf()

        btnSearch.setOnClickListener {
//            myAdapter.clear()
            val city: String = edtCity.text.toString()
            if (city.isEmpty()){
                Toast.makeText(this, "City must filled", Toast.LENGTH_SHORT).show()
            }
            else{
                loadWeather(city)
            }
        }

    }

    // http://api.openweathermap.org/data/2.5/weather?q=Danang&units=metric&appid=fc04615f6c90ae64cba31f1748c56873
    private fun loadWeather(city: String) {
        mList.clear()
        Ion.with(this)
            .load("http://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=${API_KEY}")
            .asJsonObject()
            .setCallback(FutureCallback<JsonObject?> { e, result ->
                // do stuff with the result or error
//                Log.i("result: ", result.toString())
                if(e!=null){
                    e.printStackTrace()
                }
                else{
                    try {
                        val main: JsonObject? = result.get("main").asJsonObject
                        val temp: Double? = main?.get("temp")?.asDouble
                        tvTemp.text = "${temp}℃"

                        val sys: JsonObject? = result.get("sys").asJsonObject
                        val name: String? = sys?.get("country")?.asString
                        tvCountry.text = "$city, ${name.toString()}"

                        val weather: JsonArray? = result?.get("weather")?.asJsonArray
                        val icon: String? = weather?.get(0)?.asJsonObject?.get("icon")?.asString
                        // load image
                        Ion.with(imgStatus)
                            .load("http://openweathermap.org/img/wn/$icon@2x.png")
//                        Log.i("res", "http://openweathermap.org/img/wn/$icon@2x.png")

                        var des: String? = weather?.get(0)?.asJsonObject?.get("description")?.asString
                        des = des?.let { standardizeString(it) }
                        tvDes.text = des.toString()

                        val coord: JsonObject? = result?.get("coord")?.asJsonObject
                        val lon: String? = coord?.get("lon")?.asString
                        val lat: String? = coord?.get("lat")?.asString
//                        Log.i("lonlat", lon.toString() + lat.toString())

                        if(lon != null && lat != null){
                            loadWeatherFor(lon, lat)
                        }

                    } catch (e: Exception){
                        Toast.makeText(this, "City name is not correct!", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            })
    }

    private fun loadWeatherFor(lon: String, lat: String) {
        try {
            val apiUrl: String = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=hourly,minutely&units=metric&appid=$API_KEY"
            Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(FutureCallback<JsonObject?> { e, result ->
                    // do stuff with the result or error
                    if(e!=null){
                        e.printStackTrace()
                    }
                    else{
                        val daily: JsonArray = result.get("daily").asJsonArray
                        for (i in 0 until daily.size()){
                            val dt = daily[i].asJsonObject.get("dt").asLong
                            val save = Date(dt.toLong() * 1000)
                            val df = SimpleDateFormat("EEE, MMM, dd", Locale.ENGLISH)
                            val date: String = df.format(save)

                            val temp = daily.get(i).asJsonObject.get("temp").asJsonObject.get("day").asString + "℃"

                            val weatherArr: JsonArray? = daily[i].asJsonObject.get("weather").asJsonArray
                            val imgStatus: String? = weatherArr?.get(0)?.asJsonObject?.get("icon")?.asString
                            val desStatus: String? = weatherArr?.get(0)?.asJsonObject?.get("description")?.asString
                            if(imgStatus != null && desStatus != null){
                                val wo = WeatherObj(date, imgStatus, temp, desStatus)
                                mList.add(wo)
                            }
                            else{
                                val wo = WeatherObj(date, "", temp, "")
                                mList.add(wo)
                            }

                            myAdapter = MyArrayAdapter(this, mList)
                            lv.adapter = myAdapter
                            myAdapter.notifyDataSetChanged()
                        }

                    }
                })
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun mapping(){
        edtCity = findViewById(R.id.edt_city)
        btnSearch = findViewById(R.id.btn_search)
        imgStatus = findViewById(R.id.img_icon)
        tvTemp = findViewById(R.id.tv_temp)
        tvCountry = findViewById(R.id.tv_country)
        tvDes = findViewById(R.id.tv_description)
        lv = findViewById(R.id.lv)
    }

    private fun standardizeString(s: String): String? {
        var res = ""
        res += (s[0].toInt() - 32).toChar()
        for (i in 1 until s.length) {
            res += if (s[i - 1] == ' ') {
                val e = s[i].toInt() - 32
                e.toChar()
            } else {
                s[i]
            }
        }
        return res
    }
}