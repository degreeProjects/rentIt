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
            Log.d("ApartmentRepo", "Starting to fetch apartments from Firebase...")

            //Fetch data from Firestore using
            val snapshot = firebaseDB.collection(APARTMENTS_COLLECTION_PATH).get().await()
            Log.d("ApartmentRepo", "Firebase returned ${snapshot.documents.size} documents")

            // Map the documents and include document ID
            val apartments = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.let { data ->
                        Log.d("ApartmentRepo", "Processing document: ${doc.id}")
                        val jsonMap = data.toMutableMap()
                        jsonMap["id"] = doc.id // Add document ID from Firestore
                        Apartment.fromJson(jsonMap)
                    }
                } catch (e: Exception) {
                    Log.e("ApartmentRepo", "Error parsing document ${doc.id}: ${e.message}", e)
                    null
                }
            }
            
            Log.d("ApartmentRepo", "Successfully parsed ${apartments.size} apartments")

            // Update the Room database
            withContext(Dispatchers.IO) {
                roomDB.apartmentDao().updateAllApartments(apartments)
            }
            Log.d("ApartmentRepo", "Apartments saved to Room database")

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