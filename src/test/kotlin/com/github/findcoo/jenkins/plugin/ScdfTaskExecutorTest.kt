package com.github.findcoo.jenkins.plugin

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


internal class ScdfTaskExecutorTest {

    @Disabled("connection test")
    @Test
    internal fun executeTask() {
        ScdfTaskExecutor().executeTask("https://", "clientId", "password", "scope", "https://", "taskName", null, null)
    }
}