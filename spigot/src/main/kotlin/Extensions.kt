package com.atombuilt.atomkt.spigot

import com.atombuilt.atomkt.commons.ATOMBUILT_BANNER
import io.github.oshai.kotlinlogging.KLogger

public fun KLogger.logAtomBuiltBanner() {
    info { ATOMBUILT_BANNER }
}
