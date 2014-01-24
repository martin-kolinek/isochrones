package org.isochrone.util

trait NormalizeAngle {
    def normalizeAngle(angle: Double) = {
        val ret = angle % (2 * math.Pi)
        if (ret < 0) ret + 2 * math.Pi else ret
    }
}