
package utils;

import entite.Hole;
import static interfaces.Log.LOG;

public class ConvertYardsToMeters {
        final static double YARD_TO_METER = .9144;
    public static Hole convertYtoM(Hole hole) {       //LOG.debug("starting 1 conversion");
        try {
            LOG.debug("starting conversion Yards to Meters with distance = " + hole.getHoleDistance());
            if (hole.getHoleDistance() == null) {
                LOG.debug("holeDistance = null");
                return null;
            } else {
        //short s = Short.valueOf(hole.getHoleDistance()); // convert String to short
                //    LOG.debug("short s = " + s);
                //double m = (short)s; // convert short to double
                double d = Double.valueOf(hole.getHoleDistance()); // convert String to double
                    LOG.debug("Double d (yards) = " + d);
                double meters = d * YARD_TO_METER;  // voir golfInterface
                    LOG.debug("Meters = " + meters);
                hole.setHoleDistance((short) meters); // convert double to short
                    LOG.debug("ending conversion Yards to Meters with = " + hole.getHoleDistance().toString());
                return hole;
            } // end if
        } catch (NullPointerException npe) {
            String msg = "Â£Â£Â£ NullPointerException = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (NumberFormatException npe) {
            String msg = "Â£Â£ NumberFormatException = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (Exception e) {
            String msg = "Â£Â£ Exception in convert = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
   //     return null;
    } //end metho
}
