package com.nethome.iotag

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
