package com.floppa.stackcabinet.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floppa.stackcabinet.database.Resource
import com.floppa.stackcabinet.models.*
import com.floppa.stackcabinet.models.states.Connection
import com.floppa.stackcabinet.models.states.Connection.*
import com.floppa.stackcabinet.navigation.Screens
import com.floppa.stackcabinet.repository.*
import com.floppa.stackcabinet.ui.Dialog
import com.google.gson.Gson
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
    data class Success(val grid: List<Cabinet>?) : ViewStateGrid()
    data class Problem(val exception: ProblemState?) : ViewStateGrid()
}

sealed class ViewStateComponents {
    // Represents different states for the overview screen
    object Loading : ViewStateComponents()
    data class Success(val components: List<Component>?) : ViewStateComponents()
    data class Problem(val exception: ProblemState?) : ViewStateComponents()
}

sealed class ViewStateScreen {
    object Grid : ViewStateScreen()
    object Components : ViewStateScreen()
    object Settings: ViewStateScreen()
}

data class UiStateGrid(
    val currentOffset: Offset = Offset.Zero,
    val currentZoom: Float = 1f,
)

@HiltViewModel
class GridViewModel
@Inject constructor(
    @ApplicationContext appContext: Context,
    private val componentsRepository: DatabaseRepository,
) : ViewModel() {

    /**
     * States of the Bluetooth connection
     */
    private val _connectionState = MutableStateFlow(DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _viewStateScreen = MutableStateFlow<ViewStateScreen>(ViewStateScreen.Grid)
    val viewStateScreen = _viewStateScreen.asStateFlow()

    /**
     * UI states of the Screen Grid
     */
    private val _viewStateGrid = MutableStateFlow<ViewStateGrid>(ViewStateGrid.Loading)
    val gridViewState = _viewStateGrid.asStateFlow()

    private val _stateDialogGrid = MutableStateFlow<Dialog>(Dialog.Closed)
    val stateDialog = _stateDialogGrid.asStateFlow()

    /**
     * UI state of the view in the screen Grid,
     * here we store the offset and Zoom level of the viewed grid
     */
    private val _uiStateGrid = MutableStateFlow(UiStateGrid())
    val uiState: StateFlow<UiStateGrid> = _uiStateGrid.asStateFlow()

    /**
     * UI states of the Screen Components
     */
    private val _viewStateComponents = MutableStateFlow<ViewStateComponents>(ViewStateComponents.Loading)
    val viewStateComponents = _viewStateComponents.asStateFlow()


    private val _revealedCardIdsList = MutableStateFlow(listOf<Int>())
    val revealedCardIdsList: StateFlow<List<Int>> get() = _revealedCardIdsList

    fun onItemExpanded(componentId: Int) {
        if (_revealedCardIdsList.value.contains(componentId)) return
        _revealedCardIdsList.value = _revealedCardIdsList.value.toMutableList().also { list ->
            list.add(componentId)
        }
    }

    fun onItemCollapsed(componentId: Int) {
        if (!_revealedCardIdsList.value.contains(componentId)) return
        _revealedCardIdsList.value = _revealedCardIdsList.value.toMutableList().also { list ->
            list.remove(componentId)
        }
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what){
                MESSAGE_RECEIVED ->{
                    val result = msg.obj as List<*>
                    Log.i("MESSAGE_RECEIVED", "P1: ${result[0].toString()}, P2:${result[1].toString()}")
                    when (CommandsEnum.values()[result[0].toString().toInt()]){
                        CommandsEnum.REQUEST_GRID -> {
                            println("Parsing GRID")
                            val graph = Gson().fromJson(result[1].toString(),  Array<IntArray>::class.java)
                            calculateGrid(graph)
                        }
                        CommandsEnum.SET_COM -> TODO()
                        CommandsEnum.SET_LED -> TODO()
                        CommandsEnum.OPEN_CAB -> TODO()
                    }
                }
                IS_CONNECTED -> {
                    Log.i("IS_CONNECTED", "connection state ${msg.arg2}")
                    when (values()[msg.arg2]){
                        CONNECTING -> _connectionState.value = CONNECTING
                        CONNECTED -> _connectionState.value = CONNECTED
                        DISCONNECTED -> _connectionState.value = DISCONNECTED
                        ERROR -> _connectionState.value = ERROR
                        STREAMING -> TODO()
                    }
                }
            }
        }
    }

    private val repository = BluetoothRepository(appContext, handler)
    private val gridRepository = GridRepository()

    fun getPairedBase(): BluetoothDevice? {
        return repository.getPairedBase()
    }

    fun startConnection() {
        _connectionState.value = CONNECTING
        repository.startConnection(repository.getPairedBase())
    }

    fun stopConnection() {
        repository.stopConnection(repository.getPairedBase())
    }

    /**
     * Make a protocol call to the ESP32
     * @param command the command you want to send
     */
    fun makeCall(command: String) {
        repository.writeToStream(command.toByteArray(Charsets.UTF_8))
    }

    /**
     * Get the list of components from the Database
     */
    fun getComponents() {
        viewModelScope.launch {
            componentsRepository.getListComponents().collect {
                when (it) {
                    is Resource.Error -> _viewStateComponents.value = ViewStateComponents.Problem(it.state)
                    is Resource.Loading -> _viewStateComponents.value = ViewStateComponents.Loading
                    is Resource.Success -> _viewStateComponents.value = ViewStateComponents.Success(it.data)
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
            _revealedCardIdsList.value = _revealedCardIdsList.value.toMutableList().also { list ->
                list.remove(component.index)
            }
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
     * Store the state of the UI in the [UiStateGrid] class
     * @param offset the new offset
     * @param zoom the new zoom
     */
    fun updateUiStateGrid(offset: Offset, zoom: Float) {
        _uiStateGrid.update {
            UiStateGrid(offset, zoom)
        }
    }

    /**
     * Set the route of the app, we use a single ViewModel for 2 UI screens, here we select the one
     * we want to see.
     */
    fun setRoute(route: Screens) {
        when(route.route){
            Screens.Components.route -> _viewStateScreen.value = ViewStateScreen.Components
            Screens.Main.route -> _viewStateScreen.value = ViewStateScreen.Grid
        }
    }

    fun setStateDialog(state: Dialog){
        _stateDialogGrid.value = state
    }
}