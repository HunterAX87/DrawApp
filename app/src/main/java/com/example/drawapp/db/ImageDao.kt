package com.example.drawapp.db
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images")
    suspend fun getAllImages(): List<ImageEntity>
}