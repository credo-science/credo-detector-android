package science.credo.mobiledetector.events

data class BatteryEvent(
        var status : Int = 0,
        var isCharging : Boolean = true,
        var chargePlug : Int = 0,
        var usbCharge : Boolean = true,
        var acCharge : Boolean = true,
        var plugged : Boolean = true,
        var batteryPct : Int = 0)