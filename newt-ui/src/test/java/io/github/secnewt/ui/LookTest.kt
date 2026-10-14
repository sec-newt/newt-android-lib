package io.github.secnewt.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class LookTest {
    @Test
    fun titlesBecomeCommands() {
        assertEquals("spam-protection", NewtLook.command("Spam protection"))
        assertEquals("recents", NewtLook.command("Recents"))
        assertEquals("block-list", NewtLook.command("Block list…"))
    }
}
