package com.example.avintura.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.avintura.R
import com.example.avintura.domain.AvinturaHour
import com.example.avintura.util.getDay
import com.example.avintura.util.getHoursOfOperation

class HoursRecyclerViewAdapter(private val hoursOpen: List<AvinturaHour>) : RecyclerView.Adapter<HoursRecyclerViewAdapter.HoursItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoursItemHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.hours_item, parent, false)
        return HoursItemHolder(layout)
    }

    override fun onBindViewHolder(holder: HoursItemHolder, position: Int) {
        val hour = hoursOpen[position]
        holder.day.text = getDay(hour.day)
        holder.time.text = getHoursOfOperation(hour)
    }

    override fun getItemCount(): Int {
        return hoursOpen.size
    }

    class HoursItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val day: TextView = itemView.findViewById(R.id.day)
        val time: TextView = itemView.findViewById(R.id.time)
    }
}
