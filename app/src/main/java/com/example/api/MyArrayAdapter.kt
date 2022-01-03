package com.example.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.api.model.WeatherObj
import com.koushikdutta.ion.Ion

class MyArrayAdapter(private val context: MainActivity, private val mlist: ArrayList<WeatherObj>)
    :ArrayAdapter<WeatherObj>(context, R.layout.my_listview, mlist){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.my_listview, null)

        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val imgWea: ImageView = view.findViewById(R.id.img_weather)
        val tvTemp: TextView = view.findViewById(R.id.tv_templv)
        val tvStatus: TextView = view.findViewById(R.id.tv_statuslv)

        val weather: WeatherObj = mlist[position]

        tvDate.text = weather.date
        Ion.with(imgWea)
            .load("http://openweathermap.org/img/wn/${weather.imgStatus}@2x.png")
        tvTemp.text = weather.temp
        tvStatus.text = weather.status

        return view
    }
}