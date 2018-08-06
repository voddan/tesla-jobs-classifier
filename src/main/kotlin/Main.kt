package com.luxoft.dvodopian.tesla.classifier

import awaitString
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.experimental.runBlocking
import java.net.URL
import com.github.kittinunf.fuel.core.Request


fun main(args: Array<String>) = runBlocking {
    val careerHtml = Fuel.get("https://www.tesla.com/careers/search/?redirect=no#/").awaitString()
    val careerData = parseCareerDatafromHtml(careerHtml)

    val allJobs = careerData.jobs
    val jobs = allJobs
            .filter { it.location.country == "US" }
            .filter { it.department.name == "Engineering" }

    val requests = jobs.map { job ->
        val pageUrl = """https://www.tesla.com/careers/job/${job.codeName()}"""
        val request = Fuel.get(pageUrl)
        JobDescriptionRequest(job, request)
    }

    val jobDescriptions = requests.map { it.awaitJobDescription() }

    val orderedJobs = jobDescriptions.sortedWith(
            compareBy<TeslaJobDescription> {it.job.isHot}
                    .thenByDescending { it.includesFE }
                    .thenByDescending{ it.isQA }
                    .thenByDescending{ it.includesQA }
                    .thenBy { it.requiresDegree }
        )

    println(orderedJobs.asReversed().joinToString(separator = "\n"))
}


data class JobDescriptionRequest(val job: TeslaJob, val request: Request) {
    suspend fun awaitJobDescription(): TeslaJobDescription {
        val jobHtml = request.awaitString()
        val description = jobHtml.substringAfter("""<section class="section-jobdescription">""").substringBefore("""</section>""")
        return TeslaJobDescription(job, request.url, description)
    }
}


data class TeslaJobDescription(val job: TeslaJob, val url: URL, val description: String) {
    val isQA = "QA" in job.title
    val includesQA = "QA" in description

    val includesJava = "Java" in description
    val includesC = Regex("""\WC[^+]""") in description
    val includesCPP = "C++" in description

    val includesBE = description.contains("backend", ignoreCase = true)
    val includesFE = description.contains("frontend", ignoreCase = true)

    val aboutAutopilot = "Autopilot" in job.title
    val aboutInfotainment = "Infotainment" in job.title
    val aboutSoftware = "Software" in job.title
    val aboutCloud = "Cloud" in job.title
    val aboutNavigation = "Navigation" in job.title
    val aboutSecurity = "Security" in job.title
    val aboutDiagnostic = "Diagnostic" in job.title


    val requiresDegree = description.contains("bachelor", ignoreCase = true)
            || description.contains("master's", ignoreCase = true)
            || "CS" in description

    val keyWords = listOf(
            job.isHot to "*HOT*",
            isQA to "QA",
            includesQA to "QA?",
            includesJava to "Java",
            includesCPP to "C++",
            includesC to "C",
            includesBE to "BackEnd",
            includesFE to "FrontEnd",
            aboutAutopilot to "Autopilot",
            aboutInfotainment to "Infotainment",
            aboutCloud to "Cloud",
            aboutNavigation to "Navigation",
            requiresDegree to "CS-Degree"
    ).filter { it.first }.map {it.second}

    override fun toString() = "${job.title.padEnd(80)}; ${keyWords.toString().padEnd(60)}; $url"
}


