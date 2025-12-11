package com.devpush.features.game.data.mappers

import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType
import comdevpushcoreDatabase.Game_collection

/**
 * Maps database entity to domain model
 */
fun Game_collection.toDomain(): GameCollection = GameCollection(
    id = id,
    name = name,
    type = CollectionType.valueOf(type),
    gameIds = emptyList(), // Will be populated separately
    createdAt = created_at,
    updatedAt = updated_at,
    description = description
)

/**
 * Maps list of database entities to domain models
 */
fun List<Game_collection>.toDomainCollections(): List<GameCollection> = map { 
    it.toDomain() 
}

/**
 * Maps domain model to database parameters for creation
 */
fun GameCollection.toCreateParams(): List<Any?> = listOf(
    id,
    name,
    type.name,
    description,
    createdAt,
    updatedAt
)

/**
 * Maps domain model to database parameters for update
 */
fun GameCollection.toUpdateParams(): List<Any?> = listOf(
    name,
    description,
    updatedAt,
    id
)