package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.forecast_rv_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Adapter(var context: Context, var weatherModelArrayList: ArrayList<WeatherModel>) :RecyclerView.Adapter<Adapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forecast_rv_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
        holder.itemView.apply {
            var inputFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
            var outputFormat: SimpleDateFormat = SimpleDateFormat("hh:mm aa")

            try {
                var d: Date = inputFormat.parse(weatherModelArrayList.get(position).time)
                tvtime.text = outputFormat.format(d)
            }

            catch (e: Exception){
                e.printStackTrace()
            }

            tvtemp.text = weatherModelArrayList.get(position).temperature.plus("°C")
            tvwind.text = weatherModelArrayList.get(position).windSpeed.plus("Km/h")

            Picasso.get()
                .load("http:".plus(weatherModelArrayList.get(position).icon))
                .into(ivcondition)
        }
    }

    override fun getItemCount(): Int {
        return weatherModelArrayList.size
    }
}