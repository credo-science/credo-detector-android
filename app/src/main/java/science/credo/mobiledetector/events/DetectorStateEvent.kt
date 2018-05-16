package science.credo.mobiledetector.events

data class DetectorStateEvent(
        var running: Boolean = false,
        var cameraOn: Boolean = false,
        var temperature: Int = 0,
        var orientation: Float = 0f,
        var accX: Float = 0f,
        var accY: Float = 0f,
        var accZ: Float = 0f,
        var type: StateType = StateType.None,
        var status: String = "") {

    enum class StateType(val stateType: Int) {
        None(0),
        Normal(5),
        Warning(5),
        Error(15)
    }
}
