package com.devpush.coreNetwork.model.gameDetails

import kotlinx.serialization.Serializable

@Serializable

data class StoreDTO(
    val id: Int,
    val store: StoreXDTO,
    val url: String
)
