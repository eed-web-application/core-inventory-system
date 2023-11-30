package edu.stanford.slac.code_inventory_system.service.utility;

@FunctionalInterface
public interface CheckIdInUse {
    boolean inUse(String tagId);
}
