package com.example.android.connectedweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.util.Log
import android.widget.TextView
import com.example.android.connectedweather.data.forecast
import com.example.android.connectedweather.data.forecast_all
import com.example.android.connectedweather.data.overview
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.view.View
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {
    private var forecastDataItems: List<forecast?> = listOf()
    private lateinit var forecastListRV: RecyclerView
    private lateinit var requestQueue: RequestQueue
    private var moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<overview> =
            moshi.adapter(overview::class.java)
    private lateinit var errorState: TextView
    private lateinit var loadingState: CircularProgressIndicator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)
        loadingState = findViewById(R.id.loading_indicator)
        loadingState.visibility = View.VISIBLE
        volleyRequest()
        forecastListRV = findViewById<RecyclerView>(R.id.rv_forecast_list)
        forecastListRV.layoutManager = LinearLayoutManager(this)
        forecastListRV.adapter = ForecastAdapter(forecastDataItems,::activityHandler)
    }

    fun volleyRequest(): Unit{
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=44.5646&lon=-123.2620&appid=97a47cb2c6f46038cde0645cbf6d4224&units=imperial"
//        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=31&lon=-100&appid=97a47cb2c6f46038cde0645cbf6d4224&units=imperial"
        val req = StringRequest(
            Request.Method.GET,
            url,
            {
                val results = jsonAdapter.fromJson(it)
                Log.d("orange","SUCCESS HERE $results")
                val date = results?.list?.elementAt(0)?.date
                Log.d("orange","$date")
                forecastListRV.adapter = ForecastAdapter((results?.list ?: listOf()),::activityHandler)
                loadingState.visibility = View.INVISIBLE
            },
            {
                Log.d("orange","ERROR HERE $it")
                errorState = findViewById<TextView>(R.id.tv_search_error)
                errorState.visibility = View.VISIBLE
                loadingState.visibility = View.INVISIBLE
            }
        )
        requestQueue.add(req)
    }
    fun activityHandler(item: forecast_all){
        val intent = Intent(this,cs492weather::class.java)
        intent.putExtra("datetime",item.datetime)
        intent.putExtra("wind",item.wind)
        intent.putExtra("cloud",item.cloud)
        intent.putExtra("rain",item.rain)
        intent.putExtra("image",item.image)
        intent.putExtra("highTemp",item.highTemp)
        intent.putExtra("lowTemp",item.lowTemp)
        intent.putExtra("description",item.description)

        startActivity(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.map ->{
                Log.d("blue","MAP")
                val uri = Uri.parse("geo:44.5646,-123.2620")
                val intent: Intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}