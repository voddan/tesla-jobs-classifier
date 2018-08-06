package com.luxoft.dvodopian.tesla.classifier

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import java.util.*

// [locations, departments, regions, listings, hotjobs, countries, cities, countriesMapping, states, countriesRegion, statesCities]
data class CareerData(
        val locations: Map<String, Location>,
        val departments: Map<String, Department>,
        val listings: List<Listing>,
        val hotjobs: List<Int>
) {

    data class Department(@Json(name = "d") val name: String)
    data class Country(@Json(name = "cc") val name: String)

    data class Location(
            @Json(name = "l") val name: String,
            @Json(name = "cc") val country: String? = null)

    data class Listing(
            @Json(name = "id") val id: Int,
            @Json(name =  "t") val title: String,
            @Json(name =  "j") val type: JobType,
            @Json(name =  "l") val locationId: Int,
            @Json(name = "dp") val departmentId: Int)

    enum class JobType {
        FullTime, Temporary, Intern, FixedTerm, PermEU, Temp2FullTime;

        object converter : Converter {
            override fun canConvert(cls: Class<*>) = cls == JobType::class.java
            override fun toJson(value: Any): String = (value as JobType).name
            override fun fromJson(jv: JsonValue) = when(jv.string) {
                "Full-Time", "FULLTIME", "Full Time" -> FullTime
                "Temp" -> Temporary
                "Intern" -> Intern
                "Fixed Term"-> FixedTerm
                "Perm EU" -> PermEU
                "Temp to Full-Time" -> Temp2FullTime
                null -> throw InputMismatchException("Job type $jv must be a string!")
                else -> throw InputMismatchException("Unknow job type $jv")
            }
        }
    }
}
