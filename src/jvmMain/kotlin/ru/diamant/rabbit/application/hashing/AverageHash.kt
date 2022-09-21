package ru.diamant.rabbit.application.hashing

import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class AverageHash {
    companion object {
        fun getHash(image: Image): Long {
            val scaledImage = BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY)

            val graphics = scaledImage.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.drawImage(image, 0, 0, 8, 8, null)
            graphics.dispose()

            val pixels = IntArray(64)
            scaledImage.data.getPixels(0, 0, 8, 8, pixels)

            val average: Int = pixels.sum() / 64

            var hash: Long = 0
            for (pixel in pixels) {
                hash = hash shl 1
                if (pixel > average) {
                    hash = hash or 1
                }
            }

            return hash
        }
    }
}
