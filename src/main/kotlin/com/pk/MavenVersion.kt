package com.pk

import okhttp3.Request

class MavenVersion(
    groupId: String,
    artifactId: String,
) {
    private val url = "https://repo.maven.apache.org/maven2/${groupId.replace(".", "/")}/$artifactId/maven-metadata.xml"

    fun getLatest(): String {
        val req =
            Request
                .Builder()
                .get()
                .url(url)
                .build()
        val resp = Http.exec(req)
        assert(resp.successful) { "req failed with ${resp.code}" }

        val release = Regex("<release>(.+?)</release>").find(resp.body)?.groupValues?.get(1)
        return release ?: Regex("<latest>(.+?)</latest>").find(resp.body)?.groupValues?.get(1)
            ?: error("could not find a release version in metadata for $url")
    }
}
