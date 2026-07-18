package com.digi.digi_vamana.vamana.isolate

/**
 * VAMANA-Isolate — isolated execution environment management.
 *
 * Responsible for spinning up sandboxed contexts to detonate or observe
 * untrusted content (files, links, apps) away from the user's real session.
 *
 * Stub only — replace [VamanaIsolateStub] with a real implementation.
 */
interface VamanaIsolate {
    fun createSandbox(policy: SandboxPolicy): SandboxHandle
    fun teardownSandbox(handle: SandboxHandle)
}

data class SandboxPolicy(
    val allowNetwork: Boolean = false,
    val allowFileSystem: Boolean = false
)

data class SandboxHandle(
    val id: String,
    val active: Boolean
)

class VamanaIsolateStub : VamanaIsolate {
    override fun createSandbox(policy: SandboxPolicy): SandboxHandle {
        // TODO: integrate real isolated execution environment (e.g. work profile / process isolation).
        return SandboxHandle(id = "stub-sandbox", active = false)
    }

    override fun teardownSandbox(handle: SandboxHandle) {
        // TODO: tear down the real sandbox instance.
    }
}
