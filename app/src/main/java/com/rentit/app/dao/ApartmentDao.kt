package com.rentit.app.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rentit.app.models.apartment.Apartment

@Dao
interface ApartmentDao {
    @Query("SELECT * FROM Apartment")
    fun getAll(): LiveData<MutableList<Apartment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg apartment: Apartment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apartments: List<Apartment>)

    @Query("SELECT * FROM Apartment WHERE id = :id")
    fun getApartment(id: String): LiveData<Apartment>

    @Query("UPDATE Apartment SET liked = :liked WHERE id = :id")
    fun setApartmentLiked(id: String, liked: Boolean)

    @Query("DELETE FROM Apartment WHERE id = :id")
    fun deleteApartment(id: String)

    @Query("DELETE FROM Apartment")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateAllApartments(apartments: List<Apartment>) {
        deleteAll()
        insertAll(apartments)
    }
}