package com.floppa.stackcabinet

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


fun Context.hasPermissions(vararg permissions: String) = permissions.all { permission ->
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
