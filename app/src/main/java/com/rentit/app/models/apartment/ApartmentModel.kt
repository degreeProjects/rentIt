package com.rentit.app.models.apartment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rentit.app.models.FireStoreModel
import com.rentit.app.dao.AppLocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Singleton model class managing apartment data from Firestore and local Room database.
class ApartmentModel private constructor() {
    // Loading states for tracking data fetch operations
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

    // Retrieves all apartments from local database and refreshes from Firestore.
    // Returns LiveData for reactive UI updates.
    suspend fun getAllApartments(): LiveData<MutableList<Apartment>> {
        refreshAllApartments()
        return roomDB.apartmentDao().getAll()
    }

    // Gets a single apartment by ID from local database.
    fun getApartment(id: String): LiveData<Apartment> {
        return roomDB.apartmentDao().getApartment(id)
    }

    // Updates the liked status of an apartment in local database.
    // withContext shifting that specific work to a background thread for quick UI updates
    suspend fun setApartmentLiked(id: String, liked: Boolean) {
        withContext(Dispatchers.IO) {
            roomDB.apartmentDao().setApartmentLiked(id, liked)
        }
    }

    // Deletes an apartment from both Firestore and local database.
    // withContext shifting that specific work to a background thread for quick UI updates
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

    // Fetches all apartments from Firestore and updates local database.
    // Updates loading state throughout the process for UI feedback.
    suspend fun refreshAllApartments() {
        try {
            //Update state to loading
            apartmentsListLoadingState.postValue(LoadingState.LOADING)
            Log.d("ApartmentRepo", "Starting to fetch apartments from Firebase...")

            //Fetch data from Firestore
            val snapshot = firebaseDB.collection(APARTMENTS_COLLECTION_PATH).get().await()
            Log.d("ApartmentRepo", "Firebase returned ${snapshot.documents.size} documents")

            // Map the documents and include document ID
            val apartments = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.let { data ->
                        Log.d("ApartmentRepo", "Processing document: ${doc.id}")
                        val jsonMap = data.toMutableMap() // clone the data to add id field
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

    // Adds a new apartment to Firestore and refreshes local data.
    suspend fun addApartment(apartment: Apartment) {
        try {
            firebaseDB.collection(APARTMENTS_COLLECTION_PATH)
                .add(apartment.toJson).await()
            refreshAllApartments()
        } catch (exception: Exception) {
            throw exception
        }
    }

    // Updates an existing apartment in Firestore and refreshes local data.
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