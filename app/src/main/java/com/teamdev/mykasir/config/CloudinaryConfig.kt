package com.teamdev.mykasir.config  // atau sesuaikan dengan paketmu

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils

object CloudinaryConfig {
    val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "dw4yynobq",
            "api_key", "748256855685981",
            "api_secret", "KgWf1xViTp4rQ_t812fEK8lQ-GI"
        )
    )
}
