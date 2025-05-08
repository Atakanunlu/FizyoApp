package com.example.fizyoapp.data.repository.note


import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import com.example.fizyoapp.domain.model.note.NoteColor
import com.example.fizyoapp.domain.model.note.NoteUpdate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor() : NoteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notesCollection = firestore.collection("notes")

    override fun getNotesByPhysiotherapistId(physiotherapistId: String): Flow<Resource<List<Note>>> = flow {
        try {
            emit(Resource.Loading())
            val snapshot = notesCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .orderBy("updateDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val notes = snapshot.documents.mapNotNull { doc -> docToNote(doc) }
            emit(Resource.Success(notes))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Notlar alınırken hata oluştu", e))
        }
    }

    override fun getNoteById(noteId: String): Flow<Resource<Note>> = flow {
        try {
            emit(Resource.Loading())
            val docSnapshot = notesCollection.document(noteId).get().await()
            if (docSnapshot.exists()) {
                val note = docToNote(docSnapshot)
                emit(Resource.Success(note ?: throw Exception("Not alırken hata oluştu")))
            } else {
                emit(Resource.Error("Not bukulanamdı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Notlar alınırken hata oluştu", e))
        }
    }

    override fun createNote(note: Note): Flow<Resource<Note>> = flow {
        try {
            emit(Resource.Loading())
            val documentRef = if (note.id.isEmpty()) {
                notesCollection.document()
            } else {
                notesCollection.document(note.id)
            }
            documentRef.set(noteToMap(note)).await()
            val createdNote = note.copy(id = documentRef.id)
            emit(Resource.Success(createdNote))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Not oluşturulurken hata oluştu", e))
        }
    }

    override fun addUpdateToNote(noteId: String, update: NoteUpdate): Flow<Resource<Note>> = flow {
        try {
            emit(Resource.Loading())
            val noteDoc = notesCollection.document(noteId).get().await()
            if (!noteDoc.exists()) {
                emit(Resource.Error("Not bulunamadı"))
                return@flow
            }

            val existingUpdates = getUpdatesFromDoc(noteDoc)
            val newUpdates = existingUpdates + update

            val now = Date()
            notesCollection.document(noteId).update(
                "updates", updatesToMapList(newUpdates),
                "updateDate", now
            ).await()

            val updatedNoteDoc = notesCollection.document(noteId).get().await()
            emit(Resource.Success(docToNote(updatedNoteDoc) ?: throw Exception("Not güncellenemedi")))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Nota güncelleme ekleme hatası", e))
        }
    }

    override fun deleteNote(noteId: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            notesCollection.document(noteId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Not silinirken hata oluştu", e))
        }
    }

    override fun updateNoteUpdate(noteId: String, updateIndex: Int, newUpdate: NoteUpdate): Flow<Resource<Note>> = flow {
        try {
            emit(Resource.Loading())
            val noteDoc = notesCollection.document(noteId).get().await()
            val existingUpdates = getUpdatesFromDoc(noteDoc).toMutableList()

            if (updateIndex < 0 || updateIndex >= existingUpdates.size) {
                emit(Resource.Error("Geçersiz güncelleme indeksi"))
                return@flow
            }

            existingUpdates[updateIndex] = newUpdate

            val now = Date()
            notesCollection.document(noteId).update(
                "updates", updatesToMapList(existingUpdates),
                "updateDate", now
            ).await()

            val updatedNoteDoc = notesCollection.document(noteId).get().await()
            emit(Resource.Success(docToNote(updatedNoteDoc) ?: throw Exception("Not güncelleme hatası")))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Güncelleme düzenlenirken bir hata oluştu", e))
        }
    }

    override fun deleteNoteUpdate(noteId: String, updateIndex: Int): Flow<Resource<Note>> = flow {
        try {
            emit(Resource.Loading())
            val noteDoc = notesCollection.document(noteId).get().await()
            val existingUpdates = getUpdatesFromDoc(noteDoc).toMutableList()

            if (updateIndex < 0 || updateIndex >= existingUpdates.size) {
                emit(Resource.Error("Geçersiz güncelleme indeksi"))
                return@flow
            }

            existingUpdates.removeAt(updateIndex)

            val now = Date()
            notesCollection.document(noteId).update(
                "updates", updatesToMapList(existingUpdates),
                "updateDate", now
            ).await()

            val updatedNoteDoc = notesCollection.document(noteId).get().await()
            emit(Resource.Success(docToNote(updatedNoteDoc) ?: throw Exception("Error updating note")))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Güncelleme silinirken bir hata oluştu", e))
        }
    }

    private fun docToNote(doc: DocumentSnapshot): Note? {
        return try {
            val id = doc.id
            val physiotherapistId = doc.getString("physiotherapistId") ?: ""
            val patientName = doc.getString("patientName") ?: ""
            val title = doc.getString("title") ?: ""
            val content = doc.getString("content") ?: ""
            val creationDate = doc.getDate("creationDate") ?: Date()
            val updateDate = doc.getDate("updateDate") ?: Date()
            val colorString = doc.getString("color") ?: NoteColor.WHITE.name
            val color = try {
                NoteColor.valueOf(colorString)
            } catch (e: Exception) {
                NoteColor.WHITE
            }

            val updates = getUpdatesFromDoc(doc)

            Note(id, physiotherapistId, patientName, title, content, creationDate, updateDate, color, updates)
        } catch (e: Exception) {
            null
        }
    }

    private fun getUpdatesFromDoc(doc: DocumentSnapshot): List<NoteUpdate> {
        val updatesData = doc.get("updates") as? List<Map<String, Any>> ?: emptyList()
        return updatesData.map { updateMap ->
            val updateText = updateMap["updateText"] as? String ?: ""
            val updateDateTimestamp = updateMap["updateDate"] as? Timestamp
            val updateDate = updateDateTimestamp?.toDate() ?: Date()
            NoteUpdate(updateText, updateDate)
        }
    }

    private fun noteToMap(note: Note): Map<String, Any> {
        return mapOf(
            "physiotherapistId" to note.physiotherapistId,
            "patientName" to note.patientName,
            "title" to note.title,
            "content" to note.content,
            "creationDate" to note.creationDate,
            "updateDate" to note.updateDate,
            "color" to note.color.name,
            "updates" to updatesToMapList(note.updates)
        )
    }

    private fun updatesToMapList(updates: List<NoteUpdate>): List<Map<String, Any>> {
        return updates.map { update ->
            mapOf(
                "updateText" to update.updateText,
                "updateDate" to update.updateDate
            )
        }
    }
}