package com.haishinkit.media

import com.haishinkit.data.VideoResolution

interface VideoSource : Source {
    var resolution: VideoResolution
}
