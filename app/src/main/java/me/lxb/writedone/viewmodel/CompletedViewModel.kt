package me.lxb.writedone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.data.repository.DraftRepository
import me.lxb.writedone.data.repository.NoteRepository
import java.util.Date
import javax.inject.Inject

data class CompletedUiState(
    val notes: List<CompletedNote> = emptyList(),
    val selectedDate: Date = Date(),
)

@HiltViewModel
class CompletedViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
    val draftRepo: DraftRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CompletedUiState())
    val state: StateFlow<CompletedUiState> = _state.asStateFlow()

    init {
        loadByDate(Date())
    }

    fun selectDate(date: Date) {
        _state.update { CompletedUiState(selectedDate = date) }
        loadByDate(date)
    }

    fun updateNoteBody(id: Long, body: String) {
        viewModelScope.launch {
            noteRepo.updateBody(id, body)
            _state.update { st ->
                st.copy(notes = st.notes.map { if (it.id == id) it.copy(body = body) else it })
            }
        }
    }

    fun addNote(content: String, createdAt: Date, durationSeconds: Int) {
        if (content.isBlank()) return
        viewModelScope.launch {
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
    }

    private fun loadByDate(date: Date) {
        viewModelScope.launch {
            val notes = noteRepo.getByDate(date)
            _state.update { it.copy(notes = notes, selectedDate = date) }
        }
    }
}
