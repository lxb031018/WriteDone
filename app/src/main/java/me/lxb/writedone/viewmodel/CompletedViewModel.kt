package me.lxb.writedone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.data.repository.DraftRepository
import me.lxb.writedone.data.repository.NoteRepository
import java.util.Date

data class CompletedUiState(
    val notes: List<CompletedNote> = emptyList(),
    val selectedDate: Date = Date(),
)

class CompletedViewModel(application: Application) : AndroidViewModel(application) {
    private val noteRepo = NoteRepository(application)
    val draftRepo = DraftRepository(application)

    private val _state = MutableStateFlow(CompletedUiState())
    val state: StateFlow<CompletedUiState> = _state.asStateFlow()

    init {
        loadByDate(Date())
    }

    fun selectDate(date: Date) {
        _state.update { CompletedUiState(selectedDate = date) }
        loadByDate(date)
    }

    fun addNote(content: String, createdAt: Date, durationSeconds: Int) {
        if (content.isBlank()) return
        val note = CompletedNote(
            content = content,
            createdAt = createdAt.time,
            durationSeconds = durationSeconds,
        )
        val rowId = noteRepo.insert(note)
        val savedNote = note.copy(id = rowId)
        _state.update { st ->
            st.copy(notes = listOf(savedNote) + st.notes)
        }
    }

    private fun loadByDate(date: Date) {
        viewModelScope.launch(Dispatchers.IO) {
            val notes = noteRepo.getByDate(date)
            _state.update { it.copy(notes = notes, selectedDate = date) }
        }
    }
}
