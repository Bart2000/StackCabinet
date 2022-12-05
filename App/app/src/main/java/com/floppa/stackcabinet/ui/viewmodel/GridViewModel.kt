package com.floppa.stackcabinet.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floppa.stackcabinet.database.Resource
import com.floppa.stackcabinet.models.*
import com.floppa.stackcabinet.repository.BluetoothRepository
import com.floppa.stackcabinet.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ViewStateGrid {
    // Represents different states for the overview screen
    object Loading : ViewStateGrid()
    object Disconnected : ViewStateGrid()
    data class Success(val grid: List<Cabinet>?) : ViewStateGrid()
    data class Problem(val exception: ProblemState?) : ViewStateGrid()
}

sealed class ViewStateListComponents {
    // Represents different states for the overview screen
    object Loading : ViewStateListComponents()
    data class Success(val components: List<Component>?) : ViewStateListComponents()
    data class Problem(val exception: ProblemState?) : ViewStateListComponents()
}

data class UiState(
    val currentOffset: Offset = Offset.Zero,
    val currentZoom: Float = 1f
)

@HiltViewModel
class GridViewModel
@Inject constructor(
    @ApplicationContext appContext: Context,
    private val componentsRepository: DatabaseRepository,
) : ViewModel() {

    private val repository = BluetoothRepository(appContext)
    private val gridRepository = GridRepository()

    private val _viewStateGrid = MutableStateFlow<ViewStateGrid>(ViewStateGrid.Disconnected)
    val gridViewState = _viewStateGrid.asStateFlow()

    private val _viewStateListComponents = MutableStateFlow<ViewStateListComponents>(ViewStateListComponents.Loading)
    val componentsViewState = _viewStateListComponents.asStateFlow()

    private val _connectionState = MutableStateFlow(repository.isConnected)
    val connectionState: StateFlow<Boolean> = _connectionState

    // UI state
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()


    fun getPairedBase(): BluetoothDevice? {
        return repository.getPairedBase()
    }

    fun startConnection() {
        repository.startConnection(repository.getPairedBase())
    }

    fun stopConnection() {
        repository.stopConnection(repository.getPairedBase())
    }

    private fun writeData(command: Commands) {
        repository.writeToStream(command.name.toByteArray(Charsets.UTF_8))
    }

    /**
     * Request the grid from the ESP
     * @param command the command you want to send
     */
    fun makeCall(command: Commands) {
        writeData(command)
    }

    /**
     * Get the list of components from the Database
     */
    fun getComponents() {
        viewModelScope.launch {
            componentsRepository.getListComponents().collect {
                when (it) {
                    is Resource.Error -> _viewStateListComponents.value = ViewStateListComponents.Problem(it.state)
                    is Resource.Loading -> _viewStateListComponents.value = ViewStateListComponents.Loading
                    is Resource.Success -> _viewStateListComponents.value = ViewStateListComponents.Success(it.data)
                }
            }
        }
    }

    /**
     * Calculate all the X and Y locations of the Cabinets
     * @param graph 2D array list representing the layout
     */
    fun calculateGrid(graph: Array<IntArray>) {
        viewModelScope.launch {
            gridRepository.calculateGrid(graph).collect {
                when (it) {
                    is Resource.Error -> _viewStateGrid.value = ViewStateGrid.Problem(it.state)
                    is Resource.Loading -> _viewStateGrid.value = ViewStateGrid.Loading
                    is Resource.Success -> _viewStateGrid.value = ViewStateGrid.Success(it.data)
                }
            }
        }
    }

    /**
     * Add new component to the Database Inventory
     * @param component, item that is added
     */
    fun addComponent(component: Component) {
        viewModelScope.launch {
            componentsRepository.addComponents(component)
            getComponents()
        }
    }

    /**
     * Remove exiting component from the Database
     * @param component, item that is removed
     */
    fun removeComponent(component: Component) {
        viewModelScope.launch {
            componentsRepository.removeComponent(component)
            getComponents()
        }
    }

    /**
     * Update exiting component from the Database
     * @param component, item that is updated
     */
    fun updateComponent(component: Component) {
        viewModelScope.launch {
            componentsRepository.updateComponent(component)
            getComponents()
        }
    }

    /**
     * Store the state of the UI in the [UiState] class
     * @param offset the new offset
     * @param zoom the new zoom
     */
    fun updateUiState(offset: Offset, zoom: Float) {
        _uiState.update{
            UiState(offset,zoom)
        }
    }
}