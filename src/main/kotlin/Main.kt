package com.luxoft.dvodopian.tesla.classifier

import awaitString
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import kotlinx.coroutines.experimental.runBlocking


fun main(args: Array<String>) = runBlocking {
    // ?department=1&region=4&country=3
    val request: Request = Fuel.get("https://www.tesla.com/careers/search/?redirect=no#/")

    val html = request.awaitString()
    val careerData = parseCareerDatafromHtml(html)

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
