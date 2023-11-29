package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;

import java.util.concurrent.Callable;

public class Utility {
    static public <T> T wrapCatch(
            Callable<T> callable,
            int errorCode) {
        try {
            return callable.call();
        } catch (ControllerLogicException e) {
            throw e;
        } catch (Exception e) {
            throw ControllerLogicException.builder()
                    .errorCode(errorCode)
                    .errorMessage(e.getMessage())
                    .errorDomain(getAllMethodInCall(2))
                    .build(); // or return null, or whatever you want
        }
    }
    static public <T> T wrapCatch(
            Callable<T> callable,
            int errorCode,
            String message) {

        try {
            return callable.call();
        } catch (ControllerLogicException e) {
            throw e;
        } catch (Exception e) {
            throw ControllerLogicException.builder()
                    .errorCode(errorCode)
                    .errorMessage(message)
                    .errorDomain(getAllMethodInCall(2))
                    .build(); // or return null, or whatever you want
        }
    }

    static String getAllMethodInCall(int uptToIndex) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(int idx = stackTraceElements.length-uptToIndex; idx >0; idx--) {
            sb.append(stackTraceElements[idx].getMethodName());
            sb.append("->");
        }
        var separatorLength = "->".length();
        if(sb.length() > separatorLength){
            sb.setLength(sb.length()-separatorLength);
        }
        return sb.toString();
    }
}
