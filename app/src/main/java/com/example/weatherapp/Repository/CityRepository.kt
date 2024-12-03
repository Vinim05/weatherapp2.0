package com.example.weatherapp.Repository

import com.example.weatherapp.Server.ApiServices

class CityRepository(val api:ApiServices) {
    fun getCities(q:String, limit:Int) =
        api.getCitiesList(q, limit, "211f102946737d549e51a966e6d496ae")
}