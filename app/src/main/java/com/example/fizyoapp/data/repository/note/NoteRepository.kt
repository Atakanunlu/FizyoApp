package com.example.fizyoapp.data.repository.note

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import com.example.fizyoapp.domain.model.note.NoteUpdate
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesByPhysiotherapistId(physiotherapistId: String): Flow<Resource<List<Note>>>
    fun getNoteById(noteId: String): Flow<Resource<Note>>
    fun createNote(note: Note): Flow<Resource<Note>>
    fun addUpdateToNote(noteId: String, update: NoteUpdate): Flow<Resource<Note>>
    fun deleteNote(noteId: String): Flow<Resource<Unit>>


    fun updateNoteUpdate(noteId: String, updateIndex: Int, newUpdate: NoteUpdate): Flow<Resource<Note>>
    fun deleteNoteUpdate(noteId: String, updateIndex: Int): Flow<Resource<Note>>

    fun addImageToNote(noteId: String, imageUrl: String): Flow<Resource<Note>>
    fun addDocumentToNote(noteId: String, documentUrl: String): Flow<Resource<Note>>
    fun addImageToNoteUpdate(noteId: String, updateIndex: Int, imageUrl: String): Flow<Resource<Note>>
    fun addDocumentToNoteUpdate(noteId: String, updateIndex: Int, documentUrl: String): Flow<Resource<Note>>
}