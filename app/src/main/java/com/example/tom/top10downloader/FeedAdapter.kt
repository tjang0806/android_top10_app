package com.example.tom.top10downloader

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ViewHolder(v: View){
    val tvName: TextView = v.findViewById(R.id.tvName)
    val tvArtist: TextView = v.findViewById(R.id.tvArtist)
    val tvSummary: TextView = v.findViewById(R.id.tvSummary)
}

class FeedAdapter(context: Context, private val resource: Int, val applications: List<FeedEntry> ) : ArrayAdapter<FeedEntry>(context, resource) {
    private val TAG = "FeedAdopter"
    private val inflater = LayoutInflater.from(context)


    override  fun getCount() : Int {
        return applications.size
    }

    // read applications array item individually and populate list_record
    override  fun getView(position: Int, convertView:View?, parent: ViewGroup): View{

        Log.d(TAG, "getView called with null convertView")
        val view : View
        val viewHolder : ViewHolder

        // grabing list_record layout
        // to improve perfomance check convertView, if it is exist, use it. Otherwise create one
        if(convertView == null) {
            view = inflater.inflate(resource, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder

        } else{
            Log.d(TAG, "getView provided a convertView")
            view = convertView
            viewHolder = view.tag as ViewHolder
        }


        val currentApp = applications[position]

        viewHolder.tvName.text = currentApp.name
        viewHolder.tvArtist.text = currentApp.artist
        viewHolder.tvSummary.text = currentApp.summary

        return view
    }
}