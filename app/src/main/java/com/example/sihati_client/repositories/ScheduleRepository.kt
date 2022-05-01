package com.example.sihati_client.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.sihati_client.database.Laboratory
import com.example.sihati_client.database.Schedule
import com.example.sihati_client.database.Test
import com.example.sihati_client.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ScheduleRepository {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    var user: MutableLiveData<User> = MutableLiveData<User>()
    var schedules:  MutableLiveData<List<Schedule>?> = MutableLiveData<List<Schedule>?>()
    var allSchedules:  MutableLiveData<List<Schedule>?> = MutableLiveData<List<Schedule>?>()

    private val testRepository = TestRepository()
    private var tests: MutableLiveData<List<Test>>? = null

    private var laboratoryCollectionRef = firestore.collection("Laboratory")
    private var scheduleCollectionRef = firestore.collection("Schedule")

    var laboratory: Laboratory? = null
    var schedule: Schedule? = null

//    var mySchedule= HashMap<String,Schedule>()
//    var mylaboratory= HashMap<String,Laboratory>()

    var mylaboratory:  MutableLiveData<HashMap<String,Laboratory>?> = MutableLiveData<HashMap<String,Laboratory>?>()
    var mySchedule:  MutableLiveData<HashMap<String,Schedule>> = MutableLiveData<HashMap<String,Schedule>>()

    init {
        testRepository.getTests()
        tests = testRepository.tests
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        getProfile()
        val currentDate= LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = currentDate.format(formatter)
        getSchedules(date)
        getAllSchedules()
    }

    fun getSchedules(date:String){
        var result =0
        val list  = ArrayList<Schedule>()
        scheduleCollectionRef.whereEqualTo("date",date)
            .orderBy("time_Start", Query.Direction.ASCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, firebaseFirestoreException ->
            schedules.value = emptyList()
            list.clear()
            firebaseFirestoreException?.let{
                Log.d("exeptions","error: "+it.message.toString())
                return@addSnapshotListener
            }
            snapshot?.let{
                for(document in it){
                    for (test in tests?.value!!){
                        if(test.schedule_id==document.id && test.user_id==auth.currentUser?.uid) {
                                result = 1
                                break
                        }else result = 0
                    }
                    if(result==0
                        && document.toObject<Schedule>().person!! < document.toObject<Schedule>().limite!!) {
                        val thisSchedule :Schedule= document.toObject()
                        thisSchedule.id = document.id
                        list.add(thisSchedule)
                    }
                }
                schedules.value = list
            }
        }
    }

    fun getAllSchedules(){
        val list  = ArrayList<Schedule>()
        scheduleCollectionRef.addSnapshotListener(MetadataChanges.INCLUDE) {snapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let{
                return@addSnapshotListener
            }
            snapshot?.let{
                for(document in it){
                    list.add(document.toObject())
                }
                allSchedules.value = list
            }
        }
    }

    fun getScheduleById(uid:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val list : HashMap<String, Schedule> = mySchedule.value!!
            val querySnapshot = scheduleCollectionRef.document(uid).get().await()
            if (querySnapshot.toObject<Schedule>() != null) {
                schedule = querySnapshot.toObject<Schedule>()
                list.set(uid, querySnapshot.toObject()!!)
                Log.d("test","in the repo = "+ mySchedule.value?.get(uid)?.date.toString())
            }
            mySchedule.value = list
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("exeptions", e.message.toString())
            }
        }
    }

    fun getLaboratoryById(uid:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = laboratoryCollectionRef.document(uid).get().await()
            if (querySnapshot.toObject<Laboratory>() != null) {
                laboratory = querySnapshot.toObject<Laboratory>()
                mylaboratory.value?.set(uid, querySnapshot.toObject()!!)
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("exeptions", e.message.toString())
            }
        }
    }

    fun updateSchedule(schedule: Schedule, newSchedule: Schedule) = CoroutineScope(Dispatchers.IO).launch {
        val scheduleQuery = scheduleCollectionRef
            .whereEqualTo("date",schedule.date)
            .whereEqualTo("laboratory_id",schedule.laboratory_id)
            .whereEqualTo("limite",schedule.limite)
            .whereEqualTo("person",schedule.person)
            .whereEqualTo("time_Start",schedule.time_Start)
            .whereEqualTo("time_end",schedule.time_end)
            .get()
            .await()
        if(scheduleQuery.documents.isNotEmpty()){
            for(document in scheduleQuery){
                try {
                    scheduleCollectionRef.document(document.id).set(
                        newSchedule,
                        SetOptions.merge()
                    ).await()
                }catch (e:Exception){
                    Log.d("exeptions","error: "+e.message.toString())
                }
            }
        }else{
            Log.d("exeptions","error: the retrieving query is empty")
        }
    }

    private fun getProfile(){
        val db = FirebaseFirestore.getInstance()
        val ref = auth.currentUser?.let { db.collection("User").document(it.uid) }
        ref?.addSnapshotListener { snapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let{
                Log.d("exeptions","error: "+it.message.toString())
                return@addSnapshotListener
            }
            snapshot?.let{
                user.value = it.toObject<User>()
            }
        }
    }

}
