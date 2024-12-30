package org.softwood.taskTypes

/**
 * save class to serialised form, and rehydrate later ...
 */

trait DehydrateableTaskTrait implements Serializable {

    private transient volatile byte[] dehydratedState
    private transient volatile boolean isHydrated = true
    private Map<String, Object> data = [:]

    synchronized void dehydrate() {
        if (isHydrated) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream()
            new ObjectOutputStream(bos).writeObject(data)
            dehydratedState = bos.toByteArray()
            data = null
            isHydrated = false
        }
    }

    synchronized void rehydrate() {
        if (!isHydrated && dehydratedState) {
            ByteArrayInputStream bis = new ByteArrayInputStream(dehydratedState)
            data = new ObjectInputStream(bis).readObject() as Map
            dehydratedState = null
            isHydrated = true
        }
    }

    synchronized void setValue(String key, Object value) {
        ensureHydrated()
        data[key] = value
    }

    synchronized Object getValue(String key) {
        ensureHydrated()
        return data[key]
    }

    private void ensureHydrated() {
        if (!isHydrated) {
            rehydrate()
        }
    }
}