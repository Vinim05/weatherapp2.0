package com.example.weatherapp.Activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Adapter.ForecastAdapter
import com.example.weatherapp.R
import com.example.weatherapp.ViewModel.WeatherViewModel
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.CurrentResponseApi
import com.example.weatherapp.model.ForecastResponseAPI
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MainActivity() : AppCompatActivity(), Parcelable {
    lateinit var binding: ActivityMainBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastAdapter by lazy {ForecastAdapter()}

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {
            val lat = 41.3198
            val lon = -81.6268
            val name = "Brecksville"
            // Current Temperature
            cityTxt.text = name
            progressBar.visibility = View.VISIBLE
            weatherViewModel.loadCurrentWeather(lat, lon, "Imperial").enqueue(object :
                retrofit2.Callback<CurrentResponseApi> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<CurrentResponseApi>,
                    response: Response<CurrentResponseApi>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        progressBar.visibility = View.GONE
                        detailLayout.visibility = View.VISIBLE
                        data?.let {
                            statusTxt.text = it.weather?.get(0)?.main ?: "-"
                            windTxt.text = it.wind?.speed?.let { Math.round(it).toString() } + " Mph"
                            humidityTxt.text = it.main?.humidity?.toString() + " %"
                            currentTempTxt.text = it.main?.temp?.let { Math.round(it).toString() } + "°"
                            maxTempTxt.text = it.main?.tempMax?.let { Math.round(it).toString() } + "°"
                            minTempTxt.text = it.main?.tempMin?.let { Math.round(it).toString() } + "°"

                            val drawable = if (isNightNow()) R.drawable.night_bg
                            else {
                                setDynamicallyWallpaper(it.weather?.get(0)?.icon ?: "-")
                            }
                            bgImage.setImageResource(drawable)
                        }
                    }
                }

                override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })


            // Blue View settings

            var radius = 10f
            val decorView = window.decorView
            val rootView = (decorView.findViewById(android.R.id.content) as ViewGroup?)
            val windowBackground = decorView.background

            rootView?.let {
                blueView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blueView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blueView.clipToOutline = true
            }


            // Forecast Temperature

            weatherViewModel.loadForecastWeather(lat, lon, "Imperial").enqueue(object : retrofit2.Callback<ForecastResponseAPI>{
                override fun onResponse(
                    call: Call<ForecastResponseAPI>,
                    response: Response<ForecastResponseAPI>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        blueView.visibility = View.VISIBLE

                        data?.let {
                            forecastAdapter.differ.submitList(it.list)
                            forecastView.apply {
                                layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                                adapter = forecastAdapter
                            }

                        }

                    }
                }

                override fun onFailure(call: Call<ForecastResponseAPI>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })

        }
    }

    private fun isNightNow(): Boolean {
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> {
                R.drawable.snow_bg
            }
            "02", "03", "04" -> {
                R.drawable.cloudy_bg
            }
            "09", "10", "11" -> {
                R.drawable.rainy_bg
            }
            "13" -> {
                R.drawable.snow_bg
            }
            "50" -> {
                R.drawable.haze_bg
            }
            else -> 0
        }
    }
}
