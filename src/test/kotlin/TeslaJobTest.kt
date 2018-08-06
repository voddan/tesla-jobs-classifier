import com.github.kittinunf.fuel.Fuel
import com.luxoft.dvodopian.tesla.classifier.TeslaJob
import com.luxoft.dvodopian.tesla.classifier.jobs
import com.luxoft.dvodopian.tesla.classifier.parseCareerDatafromHtml
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.Rule



class TeslaJobTest {
    @Rule @JvmField val collector = ErrorCollector()

    lateinit var jobs: List<TeslaJob>

    @Before
    fun setUp() = runBlocking {
        val html = Fuel.get("https://www.tesla.com/careers/search/?redirect=no#/").awaitString()
        val careerData = parseCareerDatafromHtml(html)

        val allJobs = careerData.jobs
        jobs = allJobs
                .filter { it.location.country == "US" }
                .filter { it.department.name == "Engineering" }
    }

    @Test fun `codeName is guessed correctly`() = runBlocking {
        val requests = jobs.map { job ->
            val pageUrl = """https://www.tesla.com/careers/job/${job.codeName()}"""
            val request = Fuel.get(pageUrl)
            Pair(job, request)
        }

        for((job, request) in requests) {
            val (_, _, pageResult) = request.awaitStringResponse()
            val (page, err) = pageResult

            print("${job.title} ")

            try {
                Assert.assertEquals(null, err)
                Assert.assertNotEquals(null, page)
                println("OK")
            } catch (e: Exception) {
                collector.addError(e)
                println("--?--> ${job.codeName()}")
            }
        }
    }
}