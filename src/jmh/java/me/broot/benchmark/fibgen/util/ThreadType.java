package me.broot.benchmark.fibgen.util;

@SuppressWarnings("unused")
public enum ThreadType {
    PLATFORM(Thread.ofPlatform()),
    VIRTUAL(Thread.ofVirtual()),
    ;

    ThreadType(Thread.Builder threadBuilder) {
        this.threadBuilder = threadBuilder;
    }

    public Thread.Builder getThreadBuilder() {
        return threadBuilder;
    }

    private final Thread.Builder threadBuilder;
}
