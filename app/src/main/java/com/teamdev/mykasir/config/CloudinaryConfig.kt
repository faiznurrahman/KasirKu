package com.teamdev.mykasir.config

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils

object CloudinaryConfig {

    val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "dw4yynobq",
            "api_key", "748256855685981",
            "api_secret", "KgWf1xViTp4rQ_t812fEK8lQ-GI"
        )
    )

    private var isInitialized = false

    fun initCloudinary(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to "dw4yynobq",
                "api_key" to "748256855685981",
                "api_secret" to "KgWf1xViTp4rQ_t812fEK8lQ-GI"
            )
            MediaManager.init(context, config)
            isInitialized = true
        }
    }
}
