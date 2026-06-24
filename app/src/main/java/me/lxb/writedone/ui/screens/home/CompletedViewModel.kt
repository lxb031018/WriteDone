package me.lxb.writedone.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.DraftRepository
import me.lxb.writedone.domain.usecase.NoteUseCase
import java.util.Date
import javax.inject.Inject

data class CompletedUiState(
    val notes: List<CompletedNote> = emptyList(),
    val todayNotes: List<CompletedNote> = emptyList(),
    val selectedDate: Date = Date(),
)

@HiltViewModel
class CompletedViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase,
    private val draftRepo: DraftRepository,
) : ViewModel() {

    suspend fun loadDraft(): String = draftRepo.load()
    suspend fun saveDraft(text: String) = draftRepo.save(text)

    private val _state = MutableStateFlow(CompletedUiState())
    val state: StateFlow<CompletedUiState> = _state.asStateFlow()

    init {
        val today = Date()
        loadByDate(today)
        loadTodayNotes()
    }

    private fun loadTodayNotes() {
        viewModelScope.launch {
            val todayNotes = noteUseCase.getNotesByDate(Date())
            _state.update { it.copy(todayNotes = todayNotes) }
        }
    }

    fun selectDate(date: Date) {
        _state.update { it.copy(selectedDate = date) }
        loadByDate(date)
    }

    fun updateNoteBody(id: Long, body: String) {
        viewModelScope.launch {
            noteUseCase.updateNoteBody(id, body)
            _state.update { st ->
                st.copy(
                    notes = st.notes.map { if (it.id == id) it.copy(body = body) else it },
                    todayNotes = st.todayNotes.map { if (it.id == id) it.copy(body = body) else it },
                )
            }
        }
    }

    fun refresh() {
        loadByDate(_state.value.selectedDate)
        loadTodayNotes()
    }

    fun addNote(content: String, createdAt: Date, durationSeconds: Int) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val savedNote = noteUseCase.addNote(content, createdAt, durationSeconds)
            _state.update { st ->
                st.copy(
                    notes = listOf(savedNote) + st.notes,
                    todayNotes = listOf(savedNote) + st.todayNotes,
                )
            }
        }
    }

    private fun loadByDate(date: Date) {
        viewModelScope.launch {
            val notes = noteUseCase.getNotesByDate(date)
            _state.update { it.copy(notes = notes, selectedDate = date) }
        }
    }
}
