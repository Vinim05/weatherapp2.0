package com.example.weatherapp.Repository

import com.example.weatherapp.Server.ApiServices

class WeatherRepository(val api:ApiServices) {
    fun getCurrentWeather(lat: Double, lng:Double, unit:String)=
        api.getCurrentWeather(lat,lng,unit,"211f102946737d549e51a966e6d496ae")
}