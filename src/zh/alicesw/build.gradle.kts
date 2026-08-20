import io.github.keiyoushi.gradle.api.ContentWarning

plugins {
    alias(ns.plugins.extension)
}

keiyoushi {
    name = "AliceSW (爱丽丝书屋)"
    versionCode = 1
    contentWarning = ContentWarning.NSFW
    libVersion = "1.6"

    source {
        lang = "zh"
        baseUrl = "https://www.alicesw.com"
    }

    deeplink {
        host("www.alicesw.com")
        path("/novel/..*")
    }
}
