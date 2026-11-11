package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FF Genius AI", appName)
  }

  @Test
  fun `session manager handles login and clear`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = SessionManager(context)
    sessionManager.clearSession()
    assertFalse(sessionManager.isLoggedIn())

    sessionManager.saveSession(101L, "pro_gamer", "pro@example.com")
    assertTrue(sessionManager.isLoggedIn())
    assertEquals(101L, sessionManager.getUserId())
    assertEquals("pro_gamer", sessionManager.getUsername())

    sessionManager.clearSession()
    assertFalse(sessionManager.isLoggedIn())
  }
}
