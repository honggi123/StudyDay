package com.coworkerteam.coworker.data.model.dto


import com.google.gson.annotations.SerializedName

data class MyStudy(
    @SerializedName("category")
    val category: String,
    @SerializedName("idx")
    val idx: Int,
    @SerializedName("img")
    val img: String,
    @SerializedName("introduce")
    val introduce: String,
    @SerializedName("is_leader")
    val isLeader: Boolean,
    @SerializedName("name")
    val name: String
)