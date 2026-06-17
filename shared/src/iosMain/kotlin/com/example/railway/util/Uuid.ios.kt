package com.example.railway.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
