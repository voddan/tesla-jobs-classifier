package com.luxoft.dvodopian.tesla.classifier

data class TeslaJob(
        val id: Int,
        val title: String,
        val type: CareerData.JobType,
        val location: CareerData.Location,
        val department: CareerData.Department,
        val isHot: Boolean
) {
    fun codeName(): String = title
            .toLowerCase()
            .replaceFirst(Regex("""[\s]"""), "-")
            .replace(Regex("""[\W]"""), "") + "-$id"
}

val CareerData.jobs get() = listings.map {
    val location = locations[it.locationId.toString()]!!
    val department = departments[it.departmentId.toString()]!!
    TeslaJob(it.id, it.title, it.type, location, department, it.id in hotjobs)
}
