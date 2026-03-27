package dev.androidbroadcast.claudex.data

import kotlinx.serialization.json.Json

internal val domainJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
