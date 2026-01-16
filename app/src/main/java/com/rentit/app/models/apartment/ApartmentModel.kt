package com.rentit.app.models.apartment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rentit.app.models.FireStoreModel
import com.rentit.app.dao.AppLocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ApartmentModel private constructor() {
    enum class LoadingState {
        LOADING,
        LOADED,
        ERROR
    }

    private val roomDB = AppLocalDatabase.db
    private val firebaseDB = FireStoreModel.instance.db
    val apartmentsListLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)

    companion object {
        const val APARTMENTS_COLLECTION_PATH = "apartments"
        val instance: ApartmentModel = ApartmentModel()
    }

    suspend fun getAllApartments(): LiveData<MutableList<Apartment>> {
        refreshAllApartments()
        return roomDB.apartmentDao().getAll()
    }

    fun getApartment(id: String): LiveData<Apartment> {
        return roomDB.apartmentDao().getApartment(id)
    }

    suspend fun setApartmentLiked(id: String, liked: Boolean) {
        withContext(Dispatchers.IO) {
            roomDB.apartmentDao().setApartmentLiked(id, liked)
        }
    }

    suspend fun deleteApartment(id: String) {
        try {
            firebaseDB.collection(APARTMENTS_COLLECTION_PATH).document(id).delete().await()
            withContext(Dispatchers.IO) {
                roomDB.apartmentDao().deleteApartment(id)
            }
        } catch (exception: Exception) {
            throw exception
        }
    }

    suspend fun refreshAllApartments() {
        try {
            //Update state to loading
            apartmentsListLoadingState.postValue(LoadingState.LOADING)

            //Fetch data from Firestore using
            val snapshot = firebaseDB.collection(APARTMENTS_COLLECTION_PATH).get().await()

            // Map the documents
            val apartments = snapshot.documents.map { Apartment.fromJson(it.data ?: emptyMap()) }

            // Update the Room database
            roomDB.apartmentDao().updateAllApartments(apartments)

            // Finalize loading state
            apartmentsListLoadingState.postValue(LoadingState.LOADED)

        } catch (e: Exception) {
            Log.e("ApartmentRepo", "Error getting documents: ", e)
            apartmentsListLoadingState.postValue(LoadingState.ERROR)
        }
    }

    suspend fun addApartment(apartment: Apartment) {
        try {
            firebaseDB.collection(APARTMENTS_COLLECTION_PATH)
                .add(apartment.toJson).await()
            refreshAllApartments()
        } catch (exception: Exception) {
            throw exception
        }
    }

    suspend fun updateApartment(apartment: Apartment) {
        try {
            firebaseDB.collection(APARTMENTS_COLLECTION_PATH)
                .document(apartment.id)
                .set(apartment.toJson).await()
            refreshAllApartments()
        } catch (exception: Exception) {
            throw exception
        }
    }
}