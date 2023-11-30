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
                    .errorDomain(getAllMethodInCall())
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
                    .errorDomain(getAllMethodInCall())
                    .build(); // or return null, or whatever you want
        }
    }

    static String getAllMethodInCall() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(int idx = 2; idx < stackTraceElements.length; idx++) {
            if(stackTraceElements[idx].getClassName().contains("edu.stanford.slac.") &&
                    !stackTraceElements[idx].getClassName().contains("<") &&
                    !stackTraceElements[idx].getClassName().contains("$") &&
                    !stackTraceElements[idx].getMethodName().contains("<") &&
                    !stackTraceElements[idx].getMethodName().contains("$")
            ) {
                sb.append(stackTraceElements[idx].getClassName());
                sb.append("::");
                sb.append(stackTraceElements[idx].getMethodName());
                break;
            }
        }
        return sb.toString();
    }
}
