package id.doran.jete_tws_sdk

import com.bluetrum.devicemanager.models.ABDevice
import com.bluetrum.devicemanager.models.DeviceBeacon
import android.bluetooth.BluetoothDevice


class BlueDevice(deviceBeacon: DeviceBeacon) : ABDevice(deviceBeacon) {
    // Define properties for BlueDevice
    private var productId: Int = 0
    private lateinit var bleDevice: BluetoothDevice
    private lateinit var bleName: String
    private lateinit var bleAddress: String

    override fun sendRequestData(data: ByteArray?) {

    }

    override fun updateDeviceStatus(deviceBeacon: DeviceBeacon) {
        // Implement the updateDeviceStatus method
    }

    override fun getProductId(): Int {
        return productId
    }

    override fun getBleDevice(): BluetoothDevice {
        return  bleDevice
    }

    override fun getBleName(): String {
        return bleName
    }

    override fun getBleAddress(): String {
        return bleAddress
    }

    fun setBleAddress(address: String){
         bleAddress = address
    }

    override fun createBond() {
        // Implement the createBond method
    }

    override fun connect() {
        // Implement the connect method
    }

    override fun startAuth() {
        // Implement the startAuth method
    }

    override fun send(data: ByteArray): Boolean {
        // Implement the send method
        return false
    }

    override fun release() {
        // Implement the release method
    }

    override fun setConnectionStateCallback(callback: ConnectionStateCallback) {
        // Implement the setConnectionStateCallback method
    }

    override fun setDataDelegate(delegate: DataDelegate) {
        // Implement the setDataDelegate method
    }
}
