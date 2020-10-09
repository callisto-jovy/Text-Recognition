/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.setting;

import ga.abzzezz.Main;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.SettingsHolder;
import org.json.JSONObject;

public class SettingsHandler {

    public void storeSettings() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("rotX", Main.INSTANCE.getRotationHandler().getX()).put("rotY", Main.INSTANCE.getRotationHandler().getY())
                .put("port", Main.INSTANCE.getSerialHandler().getIndex())
                .put("logResultsToFile", SettingsHolder.logResultsToFile)
                .put("threshold1", Main.INSTANCE.getProcessingHandler().getThresholds()[0])
                .put("threshold2", Main.INSTANCE.getProcessingHandler().getThresholds()[1])
                .put("camIndex", Main.INSTANCE.getProcessingHandler().getCamIndex());
        FileUtil.writeStringToFile(Main.INSTANCE.getSavedFile(), jsonObject.toString(), false);
    }
}
