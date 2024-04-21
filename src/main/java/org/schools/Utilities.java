package org.schools;

import java.util.Collection;

public class Utilities {

    private Utilities(){

    }

    public static boolean isEmptyOrNull(Object object){
        if (object == null) {
            return true;
        }
        if (object instanceof String string) {
            return string.isEmpty();
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        }
        return false;
    }

}
