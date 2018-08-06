package com.luxoft.dvodopian.tesla.classifier

import awaitString
import com.beust.klaxon.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import kotlinx.coroutines.experimental.runBlocking
import java.util.*


fun main(args: Array<String>) = runBlocking {
    // ?department=1&region=4&country=3
    val request: Request = Fuel.get("https://www.tesla.com/careers/search/?redirect=no#/")

    val str = request.awaitString()
    val careersStr =str.substringAfter("window.careers = ").substringBefore(";\n    window.tesla.strings = ")

    val careerData = Klaxon()
            .converter(CareerData.JobType.converter)
            .parse<CareerData>(careersStr)!!

    val allJobs = careerData.jobs
    val jobs = allJobs
            .filter { it.location.country == "US" }
            .filter { it.department.name == "Engineering" }

    println(jobs.joinToString(separator = "\n"))

    println("Hot Jobs: \n" + jobs.filter { it.isHot }.map { it }.joinToString(separator = "\n"))

    println("Hot jobs ${careerData.hotjobs.size} out of ${jobs.size}")

}

data class TeslaJob(
        val id: Int,
        val title: String,
        val type: CareerData.JobType,
        val location: CareerData.Location,
        val department: CareerData.Department,
        val isHot: Boolean
)

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

//    @Json(ignored = true)
    val jobs get() = listings.map {
        val location = locations[it.locationId.toString()]!!
        val department = departments[it.departmentId.toString()]!!
        TeslaJob(it.id, it.title, it.type, location, department, it.id in hotjobs)
    }
}