package com.coworkerteam.coworker.data.model.other

import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse

data class CamStudyServiceData(
    val isAudio: Boolean,
    val isVideo: Boolean,
    val cameraSwith: String,
    val studyInfo: EnterCamstudyResponse,
    val timer: Int
)
