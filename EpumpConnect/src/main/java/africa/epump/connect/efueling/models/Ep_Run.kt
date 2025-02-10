package africa.epump.connect.efueling.models

import com.fuelmetrics.epumpwifitool.NativeLibJava


public class Ep_Run(nativeLibJava: NativeLibJava) : Runnable {
    private var _nativeLibJava: NativeLibJava = nativeLibJava

    override fun run() {
        var ret = 0
        do {
            ret = _nativeLibJava.ep_run()
        }while (ret == 0)
    }
}