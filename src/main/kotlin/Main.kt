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

val CareerData.jobs get() = listings.map {
    val location = locations[it.locationId.toString()]!!
    val department = departments[it.departmentId.toString()]!!
    TeslaJob(it.id, it.title, it.type, location, department, it.id in hotjobs)
}
