/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */

package ga.abzzezz.setting;

import ga.abzzezz.Singleton;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.SettingsHolder;
import org.json.JSONObject;

public class SettingsHandler {

    public void storeSettings() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("rotX", Singleton.INSTANCE.getRotationHandler().getX()).put("rotY", Singleton.INSTANCE.getRotationHandler().getY())
                .put("port", Singleton.INSTANCE.getSerialHandler().getIndex())
                .put("logResultsToFile", SettingsHolder.logResultsToFile)
                .put("threshold1", Singleton.INSTANCE.getProcessingHandler().getThresholds()[0])
                .put("threshold2", Singleton.INSTANCE.getProcessingHandler().getThresholds()[1])
                .put("camIndex", Singleton.INSTANCE.getProcessingHandler().getCamIndex());
        FileUtil.writeStringToFile(Singleton.INSTANCE.getSavedFile(), jsonObject.toString(), false);
    }
}
