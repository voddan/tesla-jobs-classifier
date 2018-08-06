package com.luxoft.dvodopian.tesla.classifier

import awaitString
import awaitStringResponse
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.experimental.runBlocking


fun main(args: Array<String>) = runBlocking {
    val html = Fuel.get("https://www.tesla.com/careers/search/?redirect=no#/").awaitString()
    val careerData = parseCareerDatafromHtml(html)

    val allJobs = careerData.jobs
    val jobs = allJobs
            .filter { it.location.country == "US" }
            .filter { it.department.name == "Engineering" }

//    println(jobs.joinToString(separator = "\n"))
//
//    println("Hot Jobs: \n" + jobs.filter { it.isHot }.map { it }.joinToString(separator = "\n"))
//    println("Hot jobs ${careerData.hotjobs.size} out of ${jobs.size}")


    val requests = jobs.map { job ->
        val pageUrl = """https://www.tesla.com/careers/job/${job.codeName()}"""
        val request = Fuel.get(pageUrl)
        Pair(job, request)
    }

    for((job, request) in requests) {
        val (_, _, pageResult) = request.awaitStringResponse()
        val (page, err) = pageResult

        print("${job.title} ")

        if(page != null)
            println("OK")
        else
            println("--?--> ${job.codeName()}")
    }
}
