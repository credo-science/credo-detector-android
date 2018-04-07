package science.credo.credomobiledetektor.events

data class DetectorStateEvent(
        var running: Boolean = false,
        var cameraOn: Boolean = false,
        var temperature: Int = 0,
        var accX: Int = 0,
        var accY: Int = 0,
        var accZ: Int = 0,
        var type: StateType = StateType.None,
        var status: String = "") {

    enum class StateType(val stateType: Int) {
        None(0),
        Normal(5),
        Warning(5),
        Error(15)
    }
}
