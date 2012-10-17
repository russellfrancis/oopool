package com.laureninnovations.util;

public interface ApplicationService {
    public void startup();
    public void shutdown();
    public void awaitShutdown();
}
