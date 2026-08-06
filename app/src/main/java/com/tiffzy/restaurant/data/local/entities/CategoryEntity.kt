package com.tiffzy.restaurant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiffzy.restaurant.data.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val image: String
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(id, name, image)
fun CategoryEntity.toDomain(): Category = Category(id, name, image)
